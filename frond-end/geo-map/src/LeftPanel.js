import React, { useState } from "react";
import { FaMapMarkerAlt, FaClock, FaCog, FaBars } from "react-icons/fa"; // Icons for menu items and breadcrumb

function LeftPanel({ activeMenu, setActiveMenu }) {
  const [isCollapsed, setIsCollapsed] = useState(false); // State to track panel collapse

  const toggleCollapse = () => {
    setIsCollapsed(!isCollapsed);
  };

  return (
    <div
      style={{
        width: isCollapsed ? "40px" : "250px",
        backgroundColor: "#1e1e1e",
        color: "#ffffff",
        padding: "10px",
        transition: "width 0.3s",
        overflow: "hidden",
      }}
    >
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: isCollapsed ? "center" : "space-between",
          marginBottom: "20px",
        }}
      >
        {!isCollapsed && <h3>Menu</h3>}
        <FaBars
          style={{ cursor: "pointer", color: "#ffffff" }}
          onClick={toggleCollapse}
          title={isCollapsed ? "Expand Menu" : "Collapse Menu"}
        />
      </div>
      <ul style={{ listStyleType: "none", padding: 0 }}>
        <li
          style={{
            display: "flex",
            alignItems: "center",
            padding: "10px",
            cursor: "pointer",
            backgroundColor: activeMenu === "All Locations" ? "#333333" : "transparent",
          }}
          onClick={() => setActiveMenu("All Locations")}
        >
          <FaMapMarkerAlt style={{ marginRight: isCollapsed ? "0" : "10px", color: "#ffffff" }} />
          {!isCollapsed && <span>All Locations</span>}
        </li>
        <li
          style={{
            display: "flex",
            alignItems: "center",
            padding: "10px",
            cursor: "pointer",
            backgroundColor: activeMenu === "Latest Locations" ? "#333333" : "transparent",
          }}
          onClick={() => setActiveMenu("Latest Locations")}
        >
          <FaClock style={{ marginRight: isCollapsed ? "0" : "10px", color: "#ffffff" }} />
          {!isCollapsed && <span>Latest Locations</span>}
        </li>
        <li
          style={{
            display: "flex",
            alignItems: "center",
            padding: "10px",
            cursor: "pointer",
            backgroundColor: activeMenu === "Settings" ? "#333333" : "transparent",
          }}
          onClick={() => setActiveMenu("Settings")}
        >
          <FaCog style={{ marginRight: isCollapsed ? "0" : "10px", color: "#ffffff" }} />
          {!isCollapsed && <span>Settings</span>}
        </li>
      </ul>
    </div>
  );
}

export default LeftPanel;