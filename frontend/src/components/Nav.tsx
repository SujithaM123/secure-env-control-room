import { NavLink } from "react-router-dom";
import { ThemeToggle } from "./ThemeToggle";

const LINKS = [
  { to: "/dev", label: "Dev" },
  { to: "/test", label: "Test" },
  { to: "/prod", label: "Prod" },
  { to: "/compare", label: "Compare" },
  { to: "/history", label: "History" },
];

export function Nav() {
  return (
    <header className="tile mb-6 flex flex-wrap items-center justify-between gap-3 rounded-sm px-4 py-3">
      <nav className="flex flex-wrap items-center gap-1">
        {LINKS.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            className={({ isActive }) =>
              `rounded-sm px-3 py-1.5 text-sm font-medium ${
                isActive ? "bg-accent text-bg" : "text-text-muted hover:text-text"
              }`
            }
          >
            {link.label}
          </NavLink>
        ))}
      </nav>
      <ThemeToggle />
    </header>
  );
}
