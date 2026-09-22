import { useState } from "react";

const fields = [
  {
    name: "studentCode",
    label: "Mã sinh viên",
    type: "text",
    required: true,
    maxLength: 20,
  },
  {
    name: "fullName",
    label: "Họ và tên",
    type: "text",
    required: true,
    maxLength: 100,
  },
  {
    name: "dateOfBirth",
    label: "Ngày sinh",
    type: "date",
  },
  {
    name: "email",
    label: "Email",
    type: "email",
    maxLength: 100,
  },
  {
    name: "phone",
    label: "Số điện thoại",
    type: "tel",
    maxLength: 15,
    pattern: "[0-9]{10,15}",
    title: "Nhập từ 10 đến 15 chữ số hoặc để trống.",
  },
];

export default function StudentForm({
  student,
  classes,
  busy,
  error,
  onSave,
  onCancel,
}) {
  const [form, setForm] = useState({
    studentCode: student?.studentCode ?? "",
    fullName: student?.fullName ?? "",
    dateOfBirth: student?.dateOfBirth ?? "",
    gender: student?.gender ?? "",
    email: student?.email ?? "",
    phone: student?.phone ?? "",
    address: student?.address ?? "",
    classId: student?.classId ? String(student.classId) : "",
  });

  const [validationError, setValidationError] = useState("");

  function handleChange(event) {
    const { name, value } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));
  }

  function handleSubmit(event) {
    event.preventDefault();
    setValidationError("");

    if (!form.studentCode.trim() || !form.fullName.trim()) {
      setValidationError("Mã sinh viên và họ tên không được để trống.");
      return;
    }

    const classId = Number(form.classId);

    if (!Number.isSafeInteger(classId) || classId <= 0) {
      setValidationError("Vui lòng chọn lớp hành chính.");
      return;
    }

    const optionalText = (value) => value.trim() || null;

    onSave({
      studentCode: form.studentCode.trim(),
      fullName: form.fullName.trim(),
      dateOfBirth: form.dateOfBirth || null,
      gender: form.gender || null,
      email: optionalText(form.email),
      phone: optionalText(form.phone),
      address: optionalText(form.address),
      classId,
    });
  }

  return (
    <section className="student-form-card">
      <h3>{student ? "Sửa thông tin sinh viên" : "Thêm sinh viên"}</h3>

      <p>Các trường có dấu * là bắt buộc.</p>

      {(validationError || error) && (
        <p className="auth-message error" role="alert">
          {validationError || error}
        </p>
      )}

      <form onSubmit={handleSubmit}>
        <fieldset disabled={busy} className="student-fieldset">
          <div className="student-form-grid">
            {fields.map((field) => (
              <div className="form-field" key={field.name}>
                <label htmlFor={`student-${field.name}`}>
                  {field.label}
                  {field.required ? " *" : ""}
                </label>

                <input
                  id={`student-${field.name}`}
                  name={field.name}
                  type={field.type}
                  value={form[field.name]}
                  onChange={handleChange}
                  required={field.required}
                  maxLength={field.maxLength}
                  pattern={field.pattern}
                  title={field.title}
                />
              </div>
            ))}

            <div className="form-field">
              <label htmlFor="student-gender">Giới tính</label>

              <select
                id="student-gender"
                name="gender"
                value={form.gender}
                onChange={handleChange}
              >
                <option value="">Chưa cập nhật</option>
                <option value="MALE">Nam</option>
                <option value="FEMALE">Nữ</option>
                <option value="OTHER">Khác</option>
              </select>
            </div>

            <div className="form-field">
              <label htmlFor="student-classId">Lớp hành chính *</label>

              <select
                id="student-classId"
                name="classId"
                value={form.classId}
                onChange={handleChange}
                required
              >
                <option value="">-- Chọn lớp --</option>

                {classes.map((item) => (
                  <option key={item.id} value={item.id}>
                    {item.classCode} - {item.className}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-field student-wide-field">
              <label htmlFor="student-address">Địa chỉ</label>

              <textarea
                id="student-address"
                name="address"
                value={form.address}
                onChange={handleChange}
                maxLength={255}
                rows={3}
              />
            </div>
          </div>

          <div className="student-actions">
            <button className="primary-button" type="submit">
              {busy ? "Đang lưu..." : "Lưu sinh viên"}
            </button>

            <button
              className="secondary-button"
              type="button"
              onClick={onCancel}
            >
              Hủy
            </button>
          </div>
        </fieldset>
      </form>
    </section>
  );
}