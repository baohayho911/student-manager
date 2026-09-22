import { useEffect, useState } from "react";
import { ApiError, getJson, sendWithCsrf } from "../api/http";
import { catalogs, lookupSources } from "../config/catalogs";
import CatalogForm from "../components/CatalogForm";
import { getLookupOptions } from "../api/catalogLookups";
import "./CatalogPage.css";

const PAGE_SIZE = 10;

function messageOf(error) {
  if (error instanceof ApiError && error.status === 401) {
    return "Phiên đăng nhập đã hết. Hãy nhấn F5 rồi đăng nhập lại.";
  }

  if (error instanceof ApiError && error.status === 403) {
    return "Yêu cầu bị từ chối. Hãy tải lại trang và kiểm tra tài khoản ADMIN.";
  }

  return error.message;
}

export default function CatalogPage() {
  const [kind, setKind] = useState("departments");
  const config = catalogs[kind];

  const [searchInput, setSearchInput] = useState("");
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(0);
  const [revision, setRevision] = useState(0);

  const [rows, setRows] = useState([]);
  const [total, setTotal] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [lookups, setLookups] = useState({});

  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  const [editor, setEditor] = useState(null);
  const [busy, setBusy] = useState(false);
  const [formError, setFormError] = useState("");
  const [actionError, setActionError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    let active = true;

    async function load() {
      setLoading(true);
      setLoadError("");

      try {
        let url = config.api;

        if (config.paginated) {
          const params = new URLSearchParams({
            keyword,
            page: String(page),
            size: String(PAGE_SIZE),
          });

          url += `?${params.toString()}`;
        }

        const sources = [
          ...new Set(
            config.fields
              .filter((field) => field.source)
              .map((field) => field.source),
          ),
        ];

        const [data, optionPairs] = await Promise.all([
          getJson(url),

          Promise.all(
            sources.map(async (sourceName) => {
  const source = lookupSources[sourceName];
  const options = await getLookupOptions(source);

  return [sourceName, options];
}),
          ),
        ]);

        let content;
        let count;
        let pages;

        if (config.paginated) {
          if (
            !Array.isArray(data?.content) ||
            !Number.isInteger(data?.totalElements) ||
            !Number.isInteger(data?.totalPages)
          ) {
            throw new Error(
  `API ${config.api} chưa trả về cấu trúc phân trang của bài 13.`,
);
          }

          content = data.content;
          count = data.totalElements;
          pages = data.totalPages;
        } else {
          if (!Array.isArray(data)) {
            throw new Error(
              `API ${config.api} chưa trả về danh sách dạng mảng.`,
            );
          }

          const normalizedKeyword = keyword.toLocaleLowerCase("vi");

          const filtered = data
            .filter((item) => {
              const text = [
                item[config.code],
                item[config.name],
              ]
                .join(" ")
                .toLocaleLowerCase("vi");

              return text.includes(normalizedKeyword);
            })
            .sort((a, b) => a.id - b.id);

          count = filtered.length;
          pages = Math.ceil(count / PAGE_SIZE);
          content = filtered.slice(
            page * PAGE_SIZE,
            (page + 1) * PAGE_SIZE,
          );
        }

        if (!active) {
          return;
        }

        if (page > 0 && page >= pages) {
          setPage(Math.max(0, pages - 1));
          return;
        }

        setRows(content);
        setTotal(count);
        setTotalPages(pages);
        setLookups(Object.fromEntries(optionPairs));
      } catch (error) {
        if (active) {
          setRows([]);
          setTotal(0);
          setTotalPages(0);
          setLoadError(messageOf(error));
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    load();

    return () => {
      active = false;
    };
  }, [config, keyword, page, revision]);

  function clearMessages() {
    setSuccess("");
    setActionError("");
    setFormError("");
  }

  function selectKind(nextKind) {
    if (busy || editor) {
      return;
    }

    setKind(nextKind);
    setSearchInput("");
    setKeyword("");
    setPage(0);
    setRows([]);
    setLookups({});
    setLoading(true);
    clearMessages();
  }

  function search(event) {
    event.preventDefault();
    setKeyword(searchInput.trim());
    setPage(0);
    setRevision((value) => value + 1);
    clearMessages();
  }

  function openEditor(item) {
    clearMessages();
    setEditor({ item });
  }

  async function save(body) {
    if (busy) {
      return;
    }

    const id = editor?.item?.id;

    setBusy(true);
    clearMessages();

    try {
      await sendWithCsrf(
        id ? `${config.api}/${id}` : config.api,
        id ? "PUT" : "POST",
        body,
      );

      setEditor(null);
      setSuccess(
        `Đã ${id ? "cập nhật" : "thêm"} ${config.title.toLowerCase()} thành công.`,
      );
      setRevision((value) => value + 1);
    } catch (error) {
      setFormError(messageOf(error));
    } finally {
      setBusy(false);
    }
  }

  async function remove(item) {
    if (busy) {
      return;
    }

    const confirmed = window.confirm(
      `Xóa ${config.title.toLowerCase()} ${item[config.code]} - ${item[config.name]}?`,
    );

    if (!confirmed) {
      return;
    }

    setBusy(true);
    clearMessages();

    try {
      await sendWithCsrf(`${config.api}/${item.id}`, "DELETE");

      setSuccess(`Đã xóa ${item[config.code]}.`);
      setRevision((value) => value + 1);
    } catch (error) {
      setActionError(messageOf(error));
    } finally {
      setBusy(false);
    }
  }

  function cellValue(item, field) {
    const value = item[field.name];

    if (value === null || value === undefined || value === "") {
      return "—";
    }

    if (field.source) {
      const option = (lookups[field.source] || []).find(
        (entry) => entry.value === String(value),
      );

      return option?.label || `ID ${value}`;
    }

    if (field.options) {
      return (
        field.options.find((entry) => entry.value === value)?.label ||
        value
      );
    }

    if (field.type === "date") {
  const parts = String(value).split("-");

  if (parts.length === 3) {
    return `${parts[2]}/${parts[1]}/${parts[0]}`;
  }
}

    return String(value);
  }

  const columns = config.columns.map((name) =>
    config.fields.find((field) => field.name === name),
  );

  const missingSource = config.fields.find(
    (field) =>
      field.source &&
      field.required &&
      (lookups[field.source] || []).length === 0,
  );

  const locked = busy || loading || editor !== null;
  const canEdit = !loadError && !missingSource;

  return (
    <section id="catalogs" className="catalog-panel">
      <h2>Quản lý danh mục</h2>

      <div className="catalog-tabs" aria-label="Chọn danh mục">
        {Object.entries(catalogs).map(([key, item]) => (
          <button
            key={key}
            type="button"
            className={kind === key ? "active" : ""}
            aria-pressed={kind === key}
            disabled={busy || editor !== null}
            onClick={() => selectKind(key)}
          >
            {item.title}
          </button>
        ))}
      </div>

      <div className="catalog-heading">
        <h3>{config.title}</h3>

        <button
          type="button"
          className="primary-button"
          disabled={locked || !canEdit}
          onClick={() => openEditor(null)}
        >
          + Thêm {config.title.toLowerCase()}
        </button>
      </div>

      {success && (
        <p className="auth-message success" role="status">
          {success}
        </p>
      )}

      {actionError && (
        <p className="auth-message error" role="alert">
          {actionError}
        </p>
      )}

      {!loading && !loadError && missingSource && (
        <p className="auth-message error" role="alert">
          Chưa có dữ liệu cho trường {missingSource.label}.
          Hãy tạo dữ liệu đó ở mục tương ứng trước.
        </p>
      )}

      {editor && (
        <CatalogForm
          key={`${kind}-${editor.item?.id ?? "new"}`}
          config={config}
          item={editor.item}
          lookups={lookups}
          busy={busy}
          error={formError}
          onSave={save}
          onCancel={() => {
            setEditor(null);
            setFormError("");
          }}
        />
      )}

      <form className="catalog-search" onSubmit={search}>
        <div className="form-field">
          <label htmlFor="catalog-keyword">
  {config.searchLabel || "Tìm theo mã hoặc tên"}
</label>

          <input
            id="catalog-keyword"
            value={searchInput}
            onChange={(event) => setSearchInput(event.target.value)}
            disabled={locked}
            placeholder="Nhập từ khóa..."
          />
        </div>

        <button
          type="submit"
          className="primary-button"
          disabled={locked}
        >
          Tìm kiếm
        </button>

        <button
          type="button"
          className="secondary-button"
          disabled={locked}
          onClick={() => {
            setSearchInput("");
            setKeyword("");
            setPage(0);
            setRevision((value) => value + 1);
            clearMessages();
          }}
        >
          Bỏ lọc
        </button>

        <button
          type="button"
          className="secondary-button"
          disabled={busy || loading}
          onClick={() => setRevision((value) => value + 1)}
        >
          Tải lại
        </button>
      </form>

      {loading ? (
        <p role="status">Đang tải dữ liệu...</p>
      ) : loadError ? (
        <p className="auth-message error" role="alert">
          {loadError}
        </p>
      ) : (
        <>
          <p>Tổng số: <strong>{total}</strong></p>

          <div className="catalog-table-wrapper">
            <table
  className={`catalog-table ${
    kind === "sections" ? "catalog-sections-table" : ""
  }`}
>
              <thead>
                <tr>
                  <th scope="col">STT</th>

                  {columns.map((field) => (
                    <th scope="col" key={field.name}>
                      {field.label}
                    </th>
                  ))}

                  <th scope="col">Thao tác</th>
                </tr>
              </thead>

              <tbody>
                {rows.map((item, index) => (
                  <tr key={item.id}>
                    <td>{page * PAGE_SIZE + index + 1}</td>

                    {columns.map((field) => (
                      <td key={field.name}>
                        {cellValue(item, field)}
                      </td>
                    ))}

                    <td>
                      <div className="catalog-actions">
                        <button
                          type="button"
                          className="secondary-button"
                          disabled={locked || !canEdit}
                          onClick={() => openEditor(item)}
                        >
                          Sửa
                        </button>

                        <button
                          type="button"
                          className="danger-button"
                          disabled={locked}
                          onClick={() => remove(item)}
                        >
                          Xóa
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}

                {rows.length === 0 && (
                  <tr>
                    <td colSpan={columns.length + 2}>
                      Không tìm thấy dữ liệu phù hợp.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          {totalPages > 0 && (
            <div className="catalog-pagination">
              <button
                type="button"
                className="secondary-button"
                disabled={locked || page === 0}
                onClick={() => setPage((value) => value - 1)}
              >
                Trang trước
              </button>

              <span>Trang {page + 1} / {totalPages}</span>

              <button
                type="button"
                className="secondary-button"
                disabled={locked || page + 1 >= totalPages}
                onClick={() => setPage((value) => value + 1)}
              >
                Trang sau
              </button>
            </div>
          )}
        </>
      )}
    </section>
  );
}