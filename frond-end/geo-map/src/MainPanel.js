import React, { useEffect, useState } from "react";
import axios from "axios";
import MapComponent from "./MapComponent";

function MainPanel({ activeMenu }) {
  const [locationData, setLocationData] = useState(null);
  const [dates, setDates] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  // Fetch geolocations from the API
  useEffect(() => {
    // Clear location data and show loader
    setLocationData(null);
    setIsLoading(true);

    axios
      .get("http://localhost:8080/api/locations") // Update with your actual API endpoint
      .then((response) => {
        setLocationData(response.data);
        if (response.data?.metadata?.dates) {
          setDates(response.data.metadata.dates);
        }
      })
      .catch((error) => {
        console.error("There was an error fetching the locations:", error);
      })
      .finally(() => {
        // Hide loader after data is fetched
        setIsLoading(false);
      });
  }, []);

  const handleDateChange = (event) => {
    const selectedDate = event.target.value;

    // Clear location data and show loader
    setLocationData(null);
    setIsLoading(true);

    axios
      .get(`http://localhost:8080/api/locations?DATE=${selectedDate}`)
      .then((response) => {
        setLocationData(response.data);
      })
      .catch((error) => {
        console.error("There was an error fetching the locations for the selected date:", error);
      })
      .finally(() => {
        // Hide loader after data is fetched
        setIsLoading(false);
      });
  };

  return (
    <div style={{ flex: 1, padding: "20px", backgroundColor: "#121212", color: "#ffffff" }}>
        {/* Data Loader Overlay */}
      {isLoading && (
        <div
          style={{
            position: "absolute",
            top: 0,
            left: 0,
            width: "100%",
            height: "100%",
            backgroundColor: "rgba(0, 0, 0, 0.7)",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            zIndex: 1000,
          }}
        >
          <h2 style={{ color: "#ffffff" }}>Loading...</h2>
        </div>
      )}

      {activeMenu === "All Locations" && (
        <>
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
            <h1 style={{ fontSize: "1.5rem" }}>GeoLocations on Map</h1>
            <select onChange={handleDateChange}
            style={{
                backgroundColor: "#1e1e1e",
                color: "#ffffff",
                border: "1px solid #333333",
                padding: "5px",
              }}
            >
              <option value="" disabled selected>
                Select a date
              </option>
              {dates.map((date, index) => (
                <option key={index} value={date}>
                  {date}
                </option>
              ))}
            </select>
          </div>
          <MapComponent locationData={locationData} />
        </>
      )}
      {activeMenu === "Latest Locations" && <h1 style={{ fontSize: "1.5rem" }}>Latest Locations</h1>}
      {activeMenu === "Settings" && <h1 style={{ fontSize: "1.5rem" }}>Settings</h1>}
    </div>
  );
}

export default MainPanel;