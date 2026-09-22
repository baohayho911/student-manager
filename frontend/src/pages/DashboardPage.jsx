import { useEffect, useState } from "react";
import { getDashboard } from "../api/dashboard";
import "./DashboardPage.css";

const numberFormatter = new Intl.NumberFormat("vi-VN");

function formatNumber(value) {
  return numberFormatter.format(value);
}

export default function DashboardPage({ role }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [revision, setRevision] = useState(0);

  useEffect(() => {
    let active = true;

    async function load() {
      setLoading(true);
      setError("");

      try {
        const result = await getDashboard(role);

        if (
          !Array.isArray(result?.metrics) ||
          !Array.isArray(result?.distribution) ||
          !Array.isArray(result?.missingClasses) ||
          !result?.stats
        ) {
          throw new Error("Dữ liệu thống kê không đúng cấu trúc.");
        }

        if (active) {
          setData(result);
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
  }, [role, revision]);

  return (
    <section id="dashboard" className="dashboard-panel">
      <div className="dashboard-heading">
        <div>
          <h2>Tổng quan</h2>
          <p>Số liệu từ dữ liệu hiện có của hệ thống.</p>
        </div>

        <button
          type="button"
          className="secondary-button"
          disabled={loading}
          onClick={() => setRevision((value) => value + 1)}
        >
          {loading ? "Đang tải..." : "Làm mới thống kê"}
        </button>
      </div>

      {loading ? (
        <p role="status">Đang tải thống kê...</p>
      ) : error ? (
        <p className="auth-message error" role="alert">
          {error}
        </p>
      ) : data ? (
        <>
          <p className="dashboard-scope">
            Phạm vi: {data.scope}
          </p>

          <div className="dashboard-metrics">
            {data.metrics.map((metric) => (
              <article className="dashboard-metric" key={metric.key}>
                <p>{metric.label}</p>
                <strong>{formatNumber(metric.value)}</strong>
              </article>
            ))}
          </div>

          <section className="dashboard-block">
            <h3>
              {role === "STUDENT"
                ? "Tình trạng điểm của tôi"
                : "Tiến độ nhập điểm"}
            </h3>

            <div className="dashboard-summary">
              <p>
                Đăng ký còn hiệu lực:
                {" "}
                <strong>{formatNumber(data.stats.enrollments)}</strong>
              </p>

              <p>
                Đã có điểm:
                {" "}
                <strong>{formatNumber(data.stats.graded)}</strong>
              </p>

              <p>
                Chưa có điểm:
                {" "}
                <strong>{formatNumber(data.stats.ungraded)}</strong>
              </p>
            </div>

            {data.stats.enrollments === 0 ? (
              <p>Chưa có đăng ký còn hiệu lực để tính tỷ lệ.</p>
            ) : (
              <>
                <label htmlFor="dashboard-completion">
                  Tỷ lệ đã có điểm: {data.stats.completionRate}%
                </label>

                <progress
                  id="dashboard-completion"
                  className="dashboard-progress"
                  value={data.stats.graded}
                  max={data.stats.enrollments}
                />
              </>
            )}
          </section>

          <section className="dashboard-block">
            <h3>Phân bố điểm chữ</h3>

            <p>
              Chỉ tính các đăng ký còn hiệu lực đã có điểm tổng kết.
            </p>

            {data.stats.graded === 0 ? (
              <p>Chưa có điểm để thống kê.</p>
            ) : (
              <div className="dashboard-distribution">
                {data.distribution.map((bucket, index) => {
                  const percent = (
                    (bucket.count / data.stats.graded) * 100
                  ).toFixed(1);

                  const progressId = `grade-distribution-${index}`;

                  return (
                    <div className="dashboard-grade-row" key={bucket.letter}>
                      <label htmlFor={progressId}>
                        {bucket.letter}
                      </label>

                      <progress
                        id={progressId}
                        value={bucket.count}
                        max={data.stats.graded}
                        aria-label={`Điểm ${bucket.letter}`}
                      />

                      <span>
                        {formatNumber(bucket.count)} ({percent}%)
                      </span>
                    </div>
                  );
                })}
              </div>
            )}
          </section>

          {role !== "STUDENT" && (
            <section className="dashboard-block">
              <h3>Lớp còn thiếu điểm</h3>

              <p>
                Tối đa 10 lớp có nhiều đăng ký chưa có điểm nhất
                trong phạm vi của bạn.
              </p>

              {data.missingClasses.length === 0 ? (
                <p>Không có lớp còn thiếu điểm trong phạm vi thống kê.</p>
              ) : (
                <div className="dashboard-table-wrapper">
                  <table className="dashboard-table">
                    <thead>
                      <tr>
                        <th scope="col">Lớp học phần</th>
                        <th scope="col">Môn học</th>
                        <th scope="col">Học kỳ</th>
                        <th scope="col">Đăng ký</th>
                        <th scope="col">Đã có điểm</th>
                        <th scope="col">Chưa có điểm</th>
                      </tr>
                    </thead>

                    <tbody>
                      {data.missingClasses.map((item) => (
                        <tr key={item.sectionCode}>
                          <td>{item.sectionCode}</td>
                          <td>{item.subjectName}</td>
                          <td>{item.semester}</td>
                          <td>{formatNumber(item.enrollments)}</td>
                          <td>{formatNumber(item.graded)}</td>
                          <td>
                            <strong>{formatNumber(item.missing)}</strong>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>
          )}

          <p className="dashboard-updated">
            Cập nhật lúc:
            {" "}
            {new Date(data.generatedAt).toLocaleString("vi-VN")}
          </p>
        </>
      ) : null}
    </section>
  );
}