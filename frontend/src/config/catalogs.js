import { CLASS_API } from "../api/students";

const departmentField = {
  name: "departmentId",
  label: "Khoa",
  type: "select",
  required: true,
  numeric: true,
  source: "departments",
};

const genderField = {
  name: "gender",
  label: "Giới tính",
  type: "select",
  options: [
    { value: "MALE", label: "Nam" },
    { value: "FEMALE", label: "Nữ" },
    { value: "OTHER", label: "Khác" },
  ],
};

export const lookupSources = {
  departments: {
    url: "/api/departments",
    code: "departmentCode",
    name: "departmentName",
  },
  majors: {
    url: "/api/majors",
    code: "majorCode",
    name: "majorName",
  },
};

export const catalogs = {
  departments: {
    title: "Khoa",
    api: "/api/departments",
    code: "departmentCode",
    name: "departmentName",
    columns: ["departmentCode", "departmentName"],
    fields: [
      {
        name: "departmentCode",
        label: "Mã khoa",
        required: true,
        maxLength: 20,
      },
      {
        name: "departmentName",
        label: "Tên khoa",
        required: true,
        maxLength: 150,
      },
    ],
  },

  majors: {
    title: "Ngành",
    api: "/api/majors",
    code: "majorCode",
    name: "majorName",
    columns: ["majorCode", "majorName", "departmentId"],
    fields: [
      {
        name: "majorCode",
        label: "Mã ngành",
        required: true,
        maxLength: 20,
      },
      {
        name: "majorName",
        label: "Tên ngành",
        required: true,
        maxLength: 150,
      },
      departmentField,
    ],
  },

  classes: {
    title: "Lớp hành chính",
    api: CLASS_API,
    code: "classCode",
    name: "className",
    columns: ["classCode", "className", "courseYear", "majorId"],
    fields: [
      {
        name: "classCode",
        label: "Mã lớp",
        required: true,
        maxLength: 30,
      },
      {
        name: "className",
        label: "Tên lớp",
        required: true,
        maxLength: 150,
      },
      {
        name: "courseYear",
        label: "Năm nhập học",
        type: "number",
        required: true,
        min: 1,
      },
      {
        name: "majorId",
        label: "Ngành",
        type: "select",
        required: true,
        numeric: true,
        source: "majors",
      },
    ],
  },

  subjects: {
    title: "Môn học",
    api: "/api/subjects",
    code: "subjectCode",
    name: "subjectName",
    columns: ["subjectCode", "subjectName", "credits"],
    fields: [
      {
        name: "subjectCode",
        label: "Mã môn học",
        required: true,
        maxLength: 20,
      },
      {
        name: "subjectName",
        label: "Tên môn học",
        required: true,
        maxLength: 150,
      },
      {
        name: "credits",
        label: "Số tín chỉ",
        type: "number",
        required: true,
        min: 1,
      },
      {
        name: "description",
        label: "Mô tả",
        type: "textarea",
      },
    ],
  },

  teachers: {
    title: "Giảng viên",
    api: "/api/teachers",
    paginated: true,
    code: "teacherCode",
    name: "fullName",
    columns: [
      "teacherCode",
      "fullName",
      "email",
      "departmentId",
      "academicDegree",
    ],
    fields: [
      {
        name: "teacherCode",
        label: "Mã giảng viên",
        required: true,
        maxLength: 20,
      },
      {
        name: "fullName",
        label: "Họ và tên",
        required: true,
        maxLength: 100,
      },
      {
        name: "dateOfBirth",
        label: "Ngày sinh",
        type: "date",
      },
      genderField,
      {
        name: "email",
        label: "Email",
        type: "email",
        required: true,
        maxLength: 100,
      },
      {
        name: "phone",
        label: "Điện thoại",
        type: "tel",
        maxLength: 15,
        pattern: "[0-9]{10,15}",
      },
      {
        name: "academicDegree",
        label: "Học vị",
        maxLength: 50,
      },
      departmentField,
    ],
  },
};

// Danh sách môn học dùng trong ô chọn của lớp học phần.
lookupSources.subjects = {
  url: "/api/subjects",
  code: "subjectCode",
  name: "subjectName",
};

// Danh sách học kỳ dùng trong ô chọn của lớp học phần.
lookupSources.semesters = {
  url: "/api/semesters",
  code: "academicYear",
  name: "semesterName",
};

// API giảng viên đã có phân trang từ bài 13.
lookupSources.teachers = {
  url: "/api/teachers",
  code: "teacherCode",
  name: "fullName",
  paginated: true,
};

// Mục quản lý học kỳ.
catalogs.semesters = {
  title: "Học kỳ",
  api: "/api/semesters",
  code: "academicYear",
  name: "semesterName",
  columns: [
    "semesterName",
    "academicYear",
    "startDate",
    "endDate",
  ],
  fields: [
    {
      name: "semesterName",
      label: "Tên học kỳ",
      required: true,
      maxLength: 50,
    },
    {
      name: "academicYear",
      label: "Năm học",
      required: true,
      maxLength: 20,
    },
    {
      name: "startDate",
      label: "Ngày bắt đầu",
      type: "date",
      required: true,
    },
    {
      name: "endDate",
      label: "Ngày kết thúc",
      type: "date",
      required: true,
    },
  ],
  validate(body) {
    if (
      body.startDate &&
      body.endDate &&
      body.startDate > body.endDate
    ) {
      return "Ngày kết thúc không được trước ngày bắt đầu.";
    }

    return "";
  },
};

// Mục quản lý lớp học phần.
catalogs.sections = {
  title: "Lớp học phần",
  api: "/api/course-sections",
  paginated: true,
  code: "sectionCode",
  name: "sectionCode",
  searchLabel: "Tìm theo mã lớp học phần",
  columns: [
    "sectionCode",
    "subjectId",
    "semesterId",
    "teacherId",
    "maximumStudents",
    "status",
  ],
  fields: [
    {
      name: "sectionCode",
      label: "Mã lớp học phần",
      required: true,
      maxLength: 30,
    },
    {
      name: "subjectId",
      label: "Môn học",
      type: "select",
      required: true,
      numeric: true,
      source: "subjects",
    },
    {
      name: "semesterId",
      label: "Học kỳ",
      type: "select",
      required: true,
      numeric: true,
      source: "semesters",
    },
    {
      name: "teacherId",
      label: "Giảng viên",
      type: "select",
      numeric: true,
      source: "teachers",
    },
    {
      name: "room",
      label: "Phòng học",
      maxLength: 50,
    },
    {
      name: "schedule",
      label: "Lịch học",
      maxLength: 100,
    },
    {
      name: "maximumStudents",
      label: "Sĩ số tối đa",
      type: "number",
      required: true,
      min: 1,
      defaultValue: 40,
    },
    {
      name: "status",
      label: "Trạng thái",
      type: "select",
      required: true,
      defaultValue: "OPEN",
      options: [
        {
          value: "OPEN",
          label: "Mở đăng ký",
        },
        {
          value: "CLOSED",
          label: "Đóng đăng ký",
        },
        {
          value: "COMPLETED",
          label: "Đã hoàn thành",
        },
      ],
    },
  ],
};