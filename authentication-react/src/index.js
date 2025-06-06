import React from "react";
import ReactDOM from "react-dom/client"; // Updated import for React 18
import { BrowserRouter } from "react-router-dom";

import App from "./App";

const root = ReactDOM.createRoot(document.getElementById("root")); // Create the root
root.render(
  <BrowserRouter>
    <App />
  </BrowserRouter>
);

