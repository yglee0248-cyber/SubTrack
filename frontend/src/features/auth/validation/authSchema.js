import * as yup from "yup";

export const loginSchema = yup.object({
  email: yup.string().trim().required("이메일을 입력해주세요.").email("이메일 형식이 올바르지 않습니다."),
  password: yup.string().required("비밀번호를 입력해주세요."),
});

export const signupSchema = yup.object({
  email: yup.string().trim().required("이메일을 입력해주세요.").email("이메일 형식이 올바르지 않습니다."),
  password: yup.string().required("비밀번호를 입력해주세요.").min(8, "비밀번호는 8자 이상이어야 합니다."),
  nickname: yup
    .string()
    .trim()
    .required("닉네임을 입력해주세요.")
    .min(2, "닉네임은 2자 이상이어야 합니다.")
    .max(30, "닉네임은 30자 이하여야 합니다."),
});
