import React, { useState, useEffect } from "react";
import { Routes, Route, Link } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";
import "./App.css";

import AuthService from "./services/auth.service";

import EventBus from "./common/EventBus";

import Login from "./components/Login";
import Register from "./components/Register";
import Home from "./components/Home";
import Profile from "./components/Profile";
import BoardUser from "./components/BoardUser";
import BoardPremium from "./components/BoardPremium";
import BoardCommissioner from "./components/BoardCommissioner";
import BoardAdmin from "./components/BoardAdmin";
import ConfirmationPage from "./components/ConfirmationPage";

const App = () => {
  const [showAdminBoard, setShowAdminBoard] = useState(false);
  const [showPremiumBoard, setShowPremiumBoard] = useState(false);
  const [showCommissionerBoard, setShowCommissionerBoard] = useState(false);
  const [currentUser, setCurrentUser] = useState(undefined);

  const logOut = () => {
    AuthService.logout();
    setCurrentUser(undefined);
    setShowAdminBoard(false);
    setShowPremiumBoard(false);
    setShowCommissionerBoard(false);
  };
  
  useEffect(() => {
    const user = AuthService.getCurrentUser();

    if (user) {
      setCurrentUser(user);
      setShowPremiumBoard(user.roles.includes("ROLE_PREMIUM"));
      setShowCommissionerBoard(user.roles.includes("ROLE_COMMISSIONER"));      
      setShowAdminBoard(user.roles.includes("ROLE_ADMIN"));
    }
    EventBus.on("logout", () => {
      logOut();
    });

    return () => {
      EventBus.remove("logout");
    };
  }, []);



  return (
    <div>
      <nav className="navbar navbar-expand navbar-dark bg-dark">
        <Link to={"/"} className="navbar-brand">
          bezKoder
        </Link>
        <div className="navbar-nav mr-auto">
          <li className="nav-item">
            <Link to={"/home"} className="nav-link">
              Home
            </Link>
          </li>

          {showCommissionerBoard && (
            <li className="nav-item">
              <Link to={"/com"} className="nav-link">
                Commissioner Board
              </Link>
            </li>
          )}

          {showAdminBoard && (
            <li className="nav-item">
              <Link to={"/admin"} className="nav-link">
                Admin Board
              </Link>
            </li>
          )}

          {currentUser && (
            <li className="nav-item">
              <Link to={"/user"} className="nav-link">
                User
              </Link>
            </li>
          )}

          {showPremiumBoard && (
            <li className="nav-item">
              <Link to={"/premium"} className="nav-link">
                Premium
              </Link>
            </li>
          )}
        </div>

        {currentUser ? (
          <div className="navbar-nav ml-auto">
            <li className="nav-item">
              <Link to={"/profile"} className="nav-link">
                {currentUser.username}
              </Link>
            </li>
            <li className="nav-item">
              <a href="/login" className="nav-link" onClick={logOut}>
                LogOut
              </a>
            </li>
          </div>
        ) : (
          <div className="navbar-nav ml-auto">
            <li className="nav-item">
              <Link to={"/login"} className="nav-link">
                Login
              </Link>
            </li>

            <li className="nav-item">
              <Link to={"/register"} className="nav-link">
                Sign Up
              </Link>
            </li>
          </div>
        )}
      </nav>

      <div className="container mt-3">
        <Routes>
          <Route path="/" element={<Home/>} />
          <Route path="/home" element={<Home/>} />
          <Route path="/login" element={<Login/>} />
          <Route path="/register" element={<Register/>} />
          <Route path="/profile" element={<Profile/>} />
          <Route path="/user" element={<BoardUser/>} />
          <Route path="/com" element={<BoardCommissioner/>} />
          <Route path="/admin" element={<BoardAdmin/>} />
          <Route path="/premium" element={<BoardPremium/>} />
          <Route path="/confirm" element={<ConfirmationPage />} />
        </Routes>
      </div>
    </div>
  );
};

export default App;