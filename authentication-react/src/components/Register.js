import React, { useState } from "react";
import { Formik, Form, Field } from 'formik';
import * as Yup from 'yup';
import AuthService from "../services/auth.service";

const Register = () => {
  const [successful, setSuccessful] = useState(false);
  const [message, setMessage] = useState("");

  const validationSchema = Yup.object().shape({
    username: Yup.string()
      .min(3, "Username must be between 3 and 20 characters")
      .max(20, "Username must be between 3 and 20 characters")
      .required("This field is required!"),
    email: Yup.string()
      .email("This is not a valid email")
      .required("This field is required!"),
    password: Yup.string()
      .min(6, "Password must be between 6 and 40 characters")
      .max(40, "Password must be between 6 and 40 characters")
      .required("This field is required!"),
    roles: Yup.array()
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
          initialValues={{
            username: "",
            email: "",
            password: "",
            roles: []
          }}
          validationSchema={validationSchema}
          onSubmit={(values, { setSubmitting }) => {
            setMessage("");
            setSuccessful(false);

            AuthService.register(
              values.username,
              values.email,
              values.password,
              values.roles
            ).then(
              (response) => {
                setMessage(response.data.message);
                setSuccessful(true);
                setSubmitting(false);
              },
              (error) => {
                const resMessage =
                  (error.response &&
                    error.response.data &&
                    error.response.data.message) ||
                  error.message ||
                  error.toString();

                setMessage(resMessage);
                setSuccessful(false);
                setSubmitting(false);
              }
            );
          }}
        >
          {({ errors, touched, values, setFieldValue }) => (
            <Form>
              {!successful && (
                <div>
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
                    <label htmlFor="email">Email</label>
                    <Field
                      name="email"
                      type="email"
                      className={`form-control ${touched.email && errors.email ? 'is-invalid' : ''}`}
                    />
                    {touched.email && errors.email && (
                      <div className="alert alert-danger">{errors.email}</div>
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
                    <label>Roles</label>
                    {['user', 'admin', 'premium', 'com'].map(role => (
                      <div key={role}>
                        <label>
                          <Field
                            type="checkbox"
                            name="roles"
                            value={role}
                            checked={values.roles.includes(role)}
                            onChange={(e) => {
                              const isChecked = e.target.checked;
                              const currentRoles = [...values.roles];
                              if (isChecked) {
                                currentRoles.push(role);
                              } else {
                                const index = currentRoles.indexOf(role);
                                if (index > -1) {
                                  currentRoles.splice(index, 1);
                                }
                              }
                              setFieldValue('roles', currentRoles);
                            }}
                          />
                          {" " + role.charAt(0).toUpperCase() + role.slice(1)}
                        </label>
                      </div>
                    ))}
                  </div>

                  <div className="form-group">
                    <button type="submit" className="btn btn-primary btn-block">
                      Sign Up
                    </button>
                  </div>
                </div>
              )}

              {message && (
                <div className="form-group">
                  <div
                    className={successful ? "alert alert-success" : "alert alert-danger"}
                    role="alert"
                  >
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

export default Register;