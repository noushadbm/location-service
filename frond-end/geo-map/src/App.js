// src/App.js
import React, { useEffect, useState } from "react";
import axios from "axios";
import MapComponent from "./MapComponent";

function App() {

  const [locationData, setLocationData] = useState(null);
  const [dates, setDates] = useState([]);

  // Fetch geolocations from the API
  useEffect(() => {
    axios.get("http://localhost:8080/api/locations") // Update with your actual API endpoint
      .then((response) => {
        //console.log(response.data);
        setLocationData(response.data);
        if (response.data?.metadata?.dates) {
          setDates(response.data.metadata.dates);
        }
      })
      .catch((error) => {
        console.error("There was an error fetching the locations:", error);
      });
  }, []);

  // Handle date selection change
  const handleDateChange = (event) => {
    const selectedDate = event.target.value;
    axios
      .get(`http://localhost:8080/api/locations?DATE=${selectedDate}`) // API call with selected date
      .then((response) => {
        setLocationData(response.data); // Update location data based on selected date
      })
      .catch((error) => {
        console.error("There was an error fetching the locations for the selected date:", error);
      });
  };

  return (
    <div className="App">
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <h1>GeoLocations on Map</h1>
        <select onChange={handleDateChange}>
          <option value='' selected>
            All Dates
          </option>
          {dates.map((date, index) => (
            <option key={index} value={date}>
              {date}
            </option>
          ))}
        </select>
      </div>
      <MapComponent locationData={locationData}/>
    </div>
  );
}

export default App;
