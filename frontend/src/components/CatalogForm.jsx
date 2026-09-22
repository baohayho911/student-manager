import { useState } from "react";

export default function CatalogForm({
  config,
  item,
  lookups,
  busy,
  error,
  onSave,
  onCancel,
}) {
  const [values, setValues] = useState(() =>
    Object.fromEntries(
      config.fields.map((field) => [
        field.name,
        String(item?.[field.name] ?? field.defaultValue ?? ""),
      ]),
    ),
  );

  const [validationError, setValidationError] = useState("");

  function changeValue(event) {
    const { name, value } = event.target;

    setValues((previous) => ({
      ...previous,
      [name]: value,
    }));
  }

  function submit(event) {
    event.preventDefault();
    setValidationError("");

    const body = {};

    for (const field of config.fields) {
      const value = values[field.name].trim();

      if (field.required && !value) {
        setValidationError(`Vui lòng nhập hoặc chọn ${field.label}.`);
        return;
      }

      if (!value) {
        body[field.name] = null;
        continue;
      }

      if (field.type === "number" || field.numeric) {
        const number = Number(value);

        if (
  !Number.isFinite(number) ||
  (!field.decimal && !Number.isSafeInteger(number)) ||
  number < (field.min ?? 1) ||
  (field.max !== undefined && number > field.max)
) {
  setValidationError(
    `${field.label} không hợp lệ. Hãy kiểm tra giá trị và giới hạn cho phép.`,
  );
  return;
}

        body[field.name] = number;
      } else {
        body[field.name] = value;
      }
    }

    const customError = config.validate?.(body);

if (customError) {
  setValidationError(customError);
  return;
}

onSave(body);
  }

  return (
    <section className="catalog-form">
      <h3>
        {item ? "Sửa" : "Thêm"} {config.title.toLowerCase()}
      </h3>

      <p>Các trường có dấu * là bắt buộc.</p>

      {(validationError || error) && (
        <p className="auth-message error" role="alert">
          {validationError || error}
        </p>
      )}

      <form onSubmit={submit}>
        <fieldset disabled={busy}>
          <div className="catalog-form-grid">
            {config.fields.map((field) => {
              const options = field.source
                ? lookups[field.source] || []
                : field.options || [];

              const inputProps = {
                id: `catalog-${field.name}`,
                name: field.name,
                value: values[field.name],
                onChange: changeValue,
                required: field.required,
              };

              return (
                <div className="form-field" key={field.name}>
                  <label htmlFor={inputProps.id}>
                    {field.label}{field.required ? " *" : ""}
                  </label>

                  {field.type === "select" ? (
                    <select {...inputProps}>
                      <option value="">
  {field.required
    ? "-- Chọn --"
    : field.name === "teacherId"
      ? "Chưa phân công"
      : "Chưa cập nhật"}
</option>

                      {options.map((option) => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </select>
                  ) : field.type === "textarea" ? (
                    <textarea {...inputProps} rows={3} />
                  ) : (
                    <input
                      {...inputProps}
                      type={field.type || "text"}
                      maxLength={field.maxLength}
                      min={field.min}
max={field.max}
step={
  field.type === "number"
    ? field.step ?? 1
    : undefined
}
                      pattern={field.pattern}
                      title={
                        field.pattern
                          ? "Nhập từ 10 đến 15 chữ số hoặc để trống."
                          : undefined
                      }
                    />
                  )}
                </div>
              );
            })}
          </div>

          <div className="catalog-actions">
            <button type="submit" className="primary-button">
              {busy ? "Đang lưu..." : "Lưu"}
            </button>

            <button
              type="button"
              className="secondary-button"
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