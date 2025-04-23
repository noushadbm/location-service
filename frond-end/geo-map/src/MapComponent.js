// src/MapComponent.js
import React from "react";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import L from "leaflet";
//import axios from "axios";

// Define the MapComponent
const MapComponent = ({ locationData }) => {
  // Initial view center coordinates and zoom level
  const center = [24.469623, 54.352125]; // Default center (e.g., London)24.469623, 54.352125
  const zoom = 15;

  return (
    <div style={{ height: "80vh" }}>
      <MapContainer center={center} zoom={zoom} style={{ height: "100%", width: "100%" }}>
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        />
        {locationData && locationData.locations.map((location, index) => (
          <Marker
            key={index}
            position={[location.latitude, location.longitude]}
            icon={new L.Icon({
              iconUrl: "https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon.png",
              iconSize: [25, 41],
              iconAnchor: [12, 41],
            })}
          >
            <Popup>
              <strong>Location:</strong> {location.latitude}, {location.longitude}
              <br />
              <strong>Date-Time:</strong> {new Date(location.dateTime).toLocaleString()}
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  );
};

export default MapComponent;
