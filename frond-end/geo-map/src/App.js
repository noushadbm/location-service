// src/App.js
import React, { useEffect, useState } from "react";
import LeftPanel from "./LeftPanel";
import MainPanel from "./MainPanel";

function App() {
  const [activeMenu, setActiveMenu] = useState("All Locations");

  return (
    <div style={{ display: "flex", height: "100vh", backgroundColor: "#121212", color: "#ffffff" }}>
      <LeftPanel activeMenu={activeMenu} setActiveMenu={setActiveMenu} />
      <MainPanel activeMenu={activeMenu} />
    </div>
  );
}

export default App;
