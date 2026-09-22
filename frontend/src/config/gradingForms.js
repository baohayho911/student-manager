export const policyForm = {
  title: "tỷ trọng điểm",
  fields: [
    {
      name: "tx1Weight",
      label: "TX1 (%)",
      type: "number",
      required: true,
      min: 15,
      max: 100,
    },
    {
      name: "tx2Weight",
      label: "TX2 (%)",
      type: "number",
      required: true,
      min: 15,
      max: 100,
    },
    {
      name: "kthpWeight",
      label: "KTHP (%)",
      type: "number",
      required: true,
      min: 1,
      max: 100,
    },
  ],
  validate(body) {
    if (
      body.tx1Weight + body.tx2Weight + body.kthpWeight !== 100
    ) {
      return "Tổng tỷ trọng phải bằng 100%.";
    }

    if (
      body.kthpWeight <= body.tx1Weight ||
      body.kthpWeight <= body.tx2Weight
    ) {
      return "Tỷ trọng KTHP phải lớn hơn từng tỷ trọng TX1 và TX2.";
    }

    return "";
  },
};

export const scoreForm = {
  title: "điểm",
  fields: [
    {
      name: "tx1Score",
      label: "Điểm TX1",
      type: "number",
      required: true,
      decimal: true,
      min: 0,
      max: 10,
      step: 0.01,
    },
    {
      name: "tx2Score",
      label: "Điểm TX2",
      type: "number",
      required: true,
      decimal: true,
      min: 0,
      max: 10,
      step: 0.01,
    },
    {
      name: "kthpScore",
      label: "Điểm KTHP",
      type: "number",
      required: true,
      decimal: true,
      min: 0,
      max: 10,
      step: 0.01,
    },
    {
      name: "note",
      label: "Ghi chú",
      maxLength: 255,
    },
  ],
};