import { useEffect, useState } from "react";
import { ApiError } from "../api/http";
import {
  getMyProfile,
  getOpenSections,
  getMyEnrollments,
  registerSection,
  cancelMyEnrollment,
} from "../api/studentPortal";
import "./StudentPortalPage.css";

const enrollmentLabels = {
  STUDYING: "Đang học",
  COMPLETED: "Đã hoàn thành",
  CANCELLED: "Đã hủy",
};

const sectionLabels = {
  OPEN: "Mở đăng ký",
  CLOSED: "Đóng đăng ký",
  COMPLETED: "Đã hoàn thành",
};

const genderLabels = {
  MALE: "Nam",
  FEMALE: "Nữ",
  OTHER: "Khác",
};

function messageOf(error) {
  if (error instanceof ApiError && error.status === 401) {
    return "Phiên đăng nhập đã hết. Hãy nhấn F5 rồi đăng nhập lại.";
  }

  if (error instanceof ApiError && error.status === 403) {
    return (
      error.message ||
      "Không có quyền truy cập. Hãy kiểm tra tài khoản sinh viên."
    );
  }

  return error.message;
}

function displayDate(value) {
  if (!value) {
    return "Chưa cập nhật";
  }

  const parts = value.split("-");

  return parts.length === 3
    ? `${parts[2]}/${parts[1]}/${parts[0]}`
    : value;
}

function SectionInfo({ section }) {
  return (
    <div className="portal-section-info">
      <strong>{section.sectionCode}</strong>

      <span>
        {section.subjectCode} - {section.subjectName}
      </span>

      <small>
        {section.credits} tín chỉ · {section.semesterName}
        {" · "}
        {section.academicYear}
      </small>

      <small>Giảng viên: {section.teacherName}</small>

      <small>
        Phòng: {section.room || "Chưa cập nhật"}
        {" · "}
        Lịch: {section.schedule || "Chưa cập nhật"}
      </small>
    </div>
  );
}

export default function StudentPortalPage() {
  const [tab, setTab] = useState("profile");

  const [profile, setProfile] = useState(null);
  const [sections, setSections] = useState(null);
  const [enrollments, setEnrollments] = useState([]);

  const [keywordInput, setKeywordInput] = useState("");
  const [query, setQuery] = useState({
    keyword: "",
    page: 0,
  });

  const [revision, setRevision] = useState(0);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);

  const [loadError, setLoadError] = useState("");
  const [actionError, setActionError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    let active = true;

    async function load() {
      setLoading(true);
      setLoadError("");

      try {
        const [profileData, sectionData, enrollmentData] =
          await Promise.all([
            getMyProfile(),
            getOpenSections(query),
            getMyEnrollments(),
          ]);

        if (
          !profileData?.studentCode ||
          !Array.isArray(sectionData?.content) ||
          !Number.isInteger(sectionData?.totalPages) ||
          !Array.isArray(enrollmentData)
        ) {
          throw new Error("Dữ liệu cổng sinh viên không đúng cấu trúc.");
        }

        if (!active) {
          return;
        }

        if (
          query.page > 0 &&
          query.page >= sectionData.totalPages
        ) {
          setQuery((previous) => ({
            ...previous,
            page: Math.max(0, sectionData.totalPages - 1),
          }));
          return;
        }

        setProfile(profileData);
        setSections(sectionData);
        setEnrollments(enrollmentData);
      } catch (error) {
        if (active) {
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
  }, [query, revision]);

  function clearMessages() {
    setSuccess("");
    setActionError("");
  }

  function search(event) {
    event.preventDefault();
    clearMessages();

    setQuery({
      keyword: keywordInput.trim(),
      page: 0,
    });
  }

  async function handleRegister(section) {
    if (busy || loading) {
      return;
    }

    const confirmed = window.confirm(
      `Đăng ký lớp ${section.sectionCode} - ${section.subjectName}?`,
    );

    if (!confirmed) {
      return;
    }

    setBusy(true);
    clearMessages();

    try {
      await registerSection(section.id);

      setSuccess(`Đã đăng ký lớp ${section.sectionCode}.`);
      setRevision((value) => value + 1);
    } catch (error) {
      setActionError(messageOf(error));
    } finally {
      setBusy(false);
    }
  }

  async function handleCancel(enrollment) {
    if (busy || loading) {
      return;
    }

    const confirmed = window.confirm(
      `Hủy đăng ký lớp ${enrollment.section.sectionCode}?`,
    );

    if (!confirmed) {
      return;
    }

    setBusy(true);
    clearMessages();

    try {
      await cancelMyEnrollment(enrollment.id);

      setSuccess(
        `Đã hủy đăng ký lớp ${enrollment.section.sectionCode}.`,
      );
      setRevision((value) => value + 1);
    } catch (error) {
      setActionError(messageOf(error));
    } finally {
      setBusy(false);
    }
  }

  const disabled = busy || loading;

  return (
    <section id="student-portal" className="portal-panel">
      <div className="portal-heading">
        <div>
          <h2>Cổng sinh viên</h2>
          <p>Hồ sơ và đăng ký học phần của bạn.</p>
        </div>

        <button
          type="button"
          className="secondary-button"
          disabled={disabled}
          onClick={() => setRevision((value) => value + 1)}
        >
          Tải lại dữ liệu
        </button>
      </div>

      <div className="portal-tabs" aria-label="Chọn nội dung">
        {[
          ["profile", "Hồ sơ của tôi"],
          ["open", "Đăng ký học phần"],
          ["enrollments", "Học phần đã đăng ký"],
        ].map(([key, label]) => (
          <button
            key={key}
            type="button"
            className={tab === key ? "active" : ""}
            aria-pressed={tab === key}
            disabled={busy}
            onClick={() => setTab(key)}
          >
            {label}
          </button>
        ))}
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

      {loading ? (
        <p role="status">Đang tải dữ liệu...</p>
      ) : loadError ? (
        <p className="auth-message error" role="alert">
          {loadError}
        </p>
      ) : (
        <>
          {tab === "profile" && profile && (
            <dl className="portal-profile">
              {[
                ["Mã sinh viên", profile.studentCode],
                ["Họ và tên", profile.fullName],
                ["Ngày sinh", displayDate(profile.dateOfBirth)],
                ["Giới tính", genderLabels[profile.gender]],
                ["Email", profile.email],
                ["Điện thoại", profile.phone],
                ["Địa chỉ", profile.address],
                [
                  "Lớp hành chính",
                  `${profile.classCode} - ${profile.className}`,
                ],
              ].map(([label, value]) => (
                <div key={label}>
                  <dt>{label}</dt>
                  <dd>{value || "Chưa cập nhật"}</dd>
                </div>
              ))}
            </dl>
          )}

          {tab === "open" && (
            <>
              <form className="portal-search" onSubmit={search}>
                <div className="form-field">
                  <label htmlFor="portal-keyword">
                    Tìm theo mã lớp học phần
                  </label>

                  <input
                    id="portal-keyword"
                    value={keywordInput}
                    onChange={(event) =>
                      setKeywordInput(event.target.value)
                    }
                    placeholder="Ví dụ: TESTHP20"
                    disabled={disabled}
                  />
                </div>

                <button
                  type="submit"
                  className="primary-button"
                  disabled={disabled}
                >
                  Tìm kiếm
                </button>

                <button
                  type="button"
                  className="secondary-button"
                  disabled={disabled}
                  onClick={() => {
                    setKeywordInput("");
                    setQuery({ keyword: "", page: 0 });
                    clearMessages();
                  }}
                >
                  Bỏ lọc
                </button>
              </form>

              <p>
                Có <strong>{sections?.totalElements ?? 0}</strong>
                {" "}lớp đang mở phù hợp.
              </p>

              <div className="portal-table-wrapper">
                <table className="portal-table">
                  <thead>
                    <tr>
                      <th scope="col">Lớp học phần</th>
                      <th scope="col">Sĩ số</th>
                      <th scope="col">Còn chỗ</th>
                      <th scope="col">Thao tác</th>
                    </tr>
                  </thead>

                  <tbody>
                    {sections?.content.map((section) => {
                      const existing = enrollments.find(
                        (item) => item.section.id === section.id,
                      );

                      const alreadyRegistered =
                        existing &&
                        existing.status !== "CANCELLED";

                      const full = section.availableSeats <= 0;

                      const label = alreadyRegistered
                        ? "Đã đăng ký"
                        : full
                          ? "Hết chỗ"
                          : existing?.status === "CANCELLED"
                            ? "Đăng ký lại"
                            : "Đăng ký";

                      return (
                        <tr key={section.id}>
                          <td>
                            <SectionInfo section={section} />
                          </td>

                          <td>
                            {section.registeredCount}
                            {" / "}
                            {section.maximumStudents}
                          </td>

                          <td>{section.availableSeats}</td>

                          <td>
                            <button
                              type="button"
                              className="primary-button"
                              disabled={
                                disabled || alreadyRegistered || full
                              }
                              onClick={() => handleRegister(section)}
                            >
                              {label}
                            </button>
                          </td>
                        </tr>
                      );
                    })}

                    {sections?.content.length === 0 && (
                      <tr>
                        <td colSpan={4}>
                          Không có lớp đang mở phù hợp.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>

              {sections && sections.totalPages > 0 && (
                <div className="portal-pagination">
                  <button
                    type="button"
                    className="secondary-button"
                    disabled={disabled || query.page === 0}
                    onClick={() =>
                      setQuery((previous) => ({
                        ...previous,
                        page: previous.page - 1,
                      }))
                    }
                  >
                    Trang trước
                  </button>

                  <span>
                    Trang {query.page + 1} / {sections.totalPages}
                  </span>

                  <button
                    type="button"
                    className="secondary-button"
                    disabled={
                      disabled ||
                      query.page + 1 >= sections.totalPages
                    }
                    onClick={() =>
                      setQuery((previous) => ({
                        ...previous,
                        page: previous.page + 1,
                      }))
                    }
                  >
                    Trang sau
                  </button>
                </div>
              )}
            </>
          )}

          {tab === "enrollments" && (
            <>
              <p>
                Danh sách gồm cả đăng ký đang học, đã hoàn thành
                và đã hủy.
              </p>

              <div className="portal-table-wrapper">
                <table className="portal-table">
                  <thead>
                    <tr>
                      <th scope="col">Lớp học phần</th>
                      <th scope="col">Trạng thái đăng ký</th>
                      <th scope="col">Trạng thái lớp</th>
                      <th scope="col">Thao tác</th>
                    </tr>
                  </thead>

                  <tbody>
                    {enrollments.map((enrollment) => {
                      const canCancel =
                        enrollment.status === "STUDYING" &&
                        enrollment.section.status === "OPEN";

                      return (
                        <tr key={enrollment.id}>
                          <td>
                            <SectionInfo section={enrollment.section} />
                          </td>

                          <td>
                            {enrollmentLabels[enrollment.status] ||
                              enrollment.status}
                          </td>

                          <td>
                            {sectionLabels[enrollment.section.status] ||
                              enrollment.section.status}
                          </td>

                          <td>
                            {canCancel ? (
                              <button
                                type="button"
                                className="danger-button"
                                disabled={disabled}
                                onClick={() => handleCancel(enrollment)}
                              >
                                Hủy đăng ký
                              </button>
                            ) : (
                              <span>—</span>
                            )}
                          </td>
                        </tr>
                      );
                    })}

                    {enrollments.length === 0 && (
                      <tr>
                        <td colSpan={4}>
                          Bạn chưa đăng ký học phần nào.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>

              <p className="portal-note">
                Yêu cầu hủy còn được backend kiểm tra.
                Đăng ký đã có điểm sẽ không được hủy.
              </p>
            </>
          )}
        </>
      )}
    </section>
  );
}