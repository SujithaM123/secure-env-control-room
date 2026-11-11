export type Theme = "dark" | "light";

const STORAGE_KEY = "cap48-theme";

export function getStoredTheme(): Theme {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    return stored === "light" ? "light" : "dark";
  } catch {
    return "dark";
  }
}

export const THEME_CHANGE_EVENT = "cap48-theme-change";

export function applyTheme(theme: Theme): void {
  document.documentElement.setAttribute("data-theme", theme);
  try {
    localStorage.setItem(STORAGE_KEY, theme);
  } catch {
    // per-viewer convenience only — fine if storage is unavailable
  }
  window.dispatchEvent(new Event(THEME_CHANGE_EVENT));
}
