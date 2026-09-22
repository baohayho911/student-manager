import { getJson } from "./http";

function toOptions(items, source) {
  return items.map((item) => ({
    value: String(item.id),
    label: `${item[source.code]} - ${item[source.name]}`,
  }));
}

export async function getLookupOptions(source) {
  // Các danh mục đang trả về mảng.
  if (!source.paginated) {
    const items = await getJson(source.url);

    if (!Array.isArray(items)) {
      throw new Error(
        `API ${source.url} chưa trả về danh sách dạng mảng.`,
      );
    }

    return toOptions(items, source);
  }

  // Các danh mục đang trả về đối tượng phân trang.
  const allItems = new Map();
  let page = 0;
  let totalPages = 1;

  while (page < totalPages) {
    const params = new URLSearchParams({
      page: String(page),
      size: "100",
    });

    const data = await getJson(
      `${source.url}?${params.toString()}`,
    );

    if (
      !Array.isArray(data?.content) ||
      !Number.isInteger(data?.totalPages) ||
      data.totalPages < 0
    ) {
      throw new Error(
        `API ${source.url} chưa trả về cấu trúc phân trang hợp lệ.`,
      );
    }

    for (const item of data.content) {
      allItems.set(item.id, item);
    }

    totalPages = data.totalPages;
    page += 1;
  }

  return toOptions([...allItems.values()], source);
}