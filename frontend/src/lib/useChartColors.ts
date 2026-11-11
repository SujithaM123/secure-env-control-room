import { useEffect, useState } from "react";
import { getChartColors } from "./chartColors";
import { THEME_CHANGE_EVENT } from "./theme";

/** Re-reads the Signal theme's literal colors whenever the light/dark toggle fires. */
export function useChartColors() {
  const [colors, setColors] = useState(getChartColors);

  useEffect(() => {
    const onChange = () => setColors(getChartColors());
    window.addEventListener(THEME_CHANGE_EVENT, onChange);
    return () => window.removeEventListener(THEME_CHANGE_EVENT, onChange);
  }, []);

  return colors;
}
