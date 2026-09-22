import { useEffect, useState } from "react";
import { getMyGradeResults } from "../api/grades";
import "./GradebookPage.css";

function displayScore(value) {
  return value === null || value === undefined ? "—" : value;
}

export default function StudentGradesPage() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [revision, setRevision] = useState(0);

  useEffect(() => {
    let active = true;

    async function load() {
      setLoading(true);
      setError("");

      try {
        const data = await getMyGradeResults();

        if (!Array.isArray(data)) {
          throw new Error("Dữ liệu kết quả học tập không đúng cấu trúc.");
        }

        if (active) {
          setRows(data);
        }
      } catch (requestError) {
        if (active) {
          setError(
            requestError.status === 401
              ? "Phiên đăng nhập đã hết. Hãy nhấn F5 rồi đăng nhập lại."
              : requestError.message,
          );
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
  }, [revision]);

  return (
    <section id="my-grades" className="grade-panel">
      <div className="grade-heading">
        <h2>Kết quả học tập của tôi</h2>

        <button
          type="button"
          className="secondary-button"
          disabled={loading}
          onClick={() => setRevision((value) => value + 1)}
        >
          Tải lại kết quả
        </button>
      </div>

      {loading ? (
        <p role="status">Đang tải kết quả...</p>
      ) : error ? (
        <p className="auth-message error" role="alert">{error}</p>
      ) : (
        <div className="grade-table-wrapper">
          <table className="grade-table">
            <thead>
              <tr>
                <th scope="col">Học phần</th>
                <th scope="col">Tín chỉ</th>
                <th scope="col">TX1</th>
                <th scope="col">TX2</th>
                <th scope="col">KTHP</th>
                <th scope="col">Tổng kết</th>
                <th scope="col">Điểm chữ</th>
                <th scope="col">Ghi chú</th>
              </tr>
            </thead>

            <tbody>
              {rows.map((row) => (
                <tr key={row.sectionCode}>
                  <td>
                    <strong>{row.subjectName}</strong>
                    <div>{row.sectionCode}</div>
                    <small>{row.semester}</small>
                  </td>

                  <td>{row.credits}</td>

                  <td>
                    {displayScore(row.score?.tx1Score)}
                    {row.score && <small> ({row.tx1Weight}%)</small>}
                  </td>

                  <td>
                    {displayScore(row.score?.tx2Score)}
                    {row.score && <small> ({row.tx2Weight}%)</small>}
                  </td>

                  <td>
                    {displayScore(row.score?.kthpScore)}
                    {row.score && <small> ({row.kthpWeight}%)</small>}
                  </td>

                  <td>{displayScore(row.score?.averageScore)}</td>
                  <td>{row.score?.letterGrade || "Chưa có điểm"}</td>
                  <td>{row.score?.note || "—"}</td>
                </tr>
              ))}

              {rows.length === 0 && (
                <tr>
                  <td colSpan={8}>
                    Bạn chưa có học phần đăng ký còn hiệu lực.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}