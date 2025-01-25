import React from "react";
import { useLocation } from "react-router-dom";

const ConfirmationPage = () => {
  const query = new URLSearchParams(useLocation().search);
  const status = query.get("status");

  const renderMessage = () => {
    switch (status) {
      case "success":
        return <h1>Your account has been confirmed! You can now log in.</h1>;
      case "invalid":
        return <h1>Invalid confirmation token. Please check your email again.</h1>;
      case "expired":
        return <h1>Your confirmation token has expired. Please request a new one.</h1>;
      default:
        return <h1>Something went wrong. Please try again later.</h1>;
    }
  };

  return <div className="confirmation-page">{renderMessage()}</div>;
};

export default ConfirmationPage;
