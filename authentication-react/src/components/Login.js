import React, { useState } from "react";
import { useNavigate } from 'react-router-dom';
import { Formik, Form, Field } from 'formik';
import * as Yup from 'yup';
import AuthService from "../services/auth.service";

const Login = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  const validationSchema = Yup.object().shape({
    username: Yup.string().required("This field is required!"),
    password: Yup.string().required("This field is required!")
  });

  return (
    <div className="col-md-12">
      <div className="card card-container">
        <img
          src="//ssl.gstatic.com/accounts/ui/avatar_2x.png"
          alt="profile-img"
          className="profile-img-card"
        />

        <Formik
          initialValues={{ username: "", password: "" }}
          validationSchema={validationSchema}
          onSubmit={(values, { setSubmitting }) => {
            setLoading(true);
            setMessage("");

            AuthService.login(values.username, values.password).then(
              () => {
                navigate("/profile");
                window.location.reload();
              },
              (error) => {
                const resMessage =
                  (error.response &&
                    error.response.data &&
                    error.response.data.message) ||
                  error.message ||
                  error.toString();

                setLoading(false);
                setMessage(resMessage);
                setSubmitting(false);
              }
            );
          }}
        >
          {({ errors, touched }) => (
            <Form>
              <div className="form-group">
                <label htmlFor="username">Username</label>
                <Field
                  name="username"
                  type="text"
                  className={`form-control ${touched.username && errors.username ? 'is-invalid' : ''}`}
                />
                {touched.username && errors.username && (
                  <div className="alert alert-danger">{errors.username}</div>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="password">Password</label>
                <Field
                  name="password"
                  type="password"
                  className={`form-control ${touched.password && errors.password ? 'is-invalid' : ''}`}
                />
                {touched.password && errors.password && (
                  <div className="alert alert-danger">{errors.password}</div>
                )}
              </div>

              <div className="form-group">
                <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
                  {loading && (
                    <span className="spinner-border spinner-border-sm"></span>
                  )}
                  <span>Login</span>
                </button>
              </div>

              {message && (
                <div className="form-group">
                  <div className="alert alert-danger" role="alert">
                    {message}
                  </div>
                </div>
              )}
            </Form>
          )}
        </Formik>
      </div>
    </div>
  );
};

export default Login;