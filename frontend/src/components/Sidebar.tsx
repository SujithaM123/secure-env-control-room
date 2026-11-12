import { NavLink } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { ThemeToggle } from "./ThemeToggle";
import { fetchAllEnvironmentSummaries } from "../lib/api";
import { healthTone } from "../lib/health";

/**
 * Each environment's own deployed host. Dev/Test/Prod links below are
 * plain <a> tags to these absolute URLs, not client-side routes -
 * confirmed live, repeatedly: a client-side route change (React Router
 * Link/NavLink) only updates the path, not the origin, so clicking "Prod"
 * while a page is being served FROM Dev's host just shows the Prod route
 * on Dev's own backend. Since every API call (including the rotate POST)
 * is same-origin, that silently checks credentials against the wrong
 * environment's secret. A real navigation to the correct host is the only
 * way to guarantee you're actually talking to that environment.
 */
const ENV_BASE_URLS: Record<string, string> = {
  dev: "http://cap48-dev.eba-3tu8qrdw.ap-south-1.elasticbeanstalk.com",
  test: "http://cap48-test.eba-3tu8qrdw.ap-south-1.elasticbeanstalk.com",
  prod: "http://cap48-prod.eba-3tu8qrdw.ap-south-1.elasticbeanstalk.com",
};

const ENV_LINKS = [
  { id: "dev", label: "Dev" },
  { id: "test", label: "Test" },
  { id: "prod", label: "Prod" },
];

const SECONDARY_LINKS = [
  { to: "/compare", label: "Compare" },
  { to: "/history", label: "History" },
];

const TONE_DOT_CLASS: Record<string, string> = {
  success: "bg-success",
  warning: "bg-warning",
  danger: "bg-danger",
  neutral: "bg-text-muted",
};

export function Sidebar() {
  const summariesQuery = useQuery({ queryKey: ["summaries"], queryFn: fetchAllEnvironmentSummaries });

  function dotToneFor(envId: string) {
    const summary = summariesQuery.data?.find((s) => s.name === envId);
    return summary ? TONE_DOT_CLASS[healthTone(summary.healthScore)] : TONE_DOT_CLASS.neutral;
  }

  const currentOrigin = typeof window !== "undefined" ? window.location.origin : "";

  return (
    <aside className="tile flex h-screen w-[170px] shrink-0 flex-col rounded-none border-y-0 border-l-0 px-3 py-4">
      <div className="mb-6 flex items-center gap-2 px-1">
        <svg viewBox="0 0 24 24" className="h-5 w-5 shrink-0 text-accent" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M12 2 L20 6 V12 C20 17 16.5 20.5 12 22 C7.5 20.5 4 17 4 12 V6 Z" />
          <path d="m9 12 2 2 4-4" />
        </svg>
        <span className="text-sm font-semibold text-text">cap-48</span>
      </div>

      <nav className="flex flex-col gap-1">
        {ENV_LINKS.map((link) => {
          const isActive = ENV_BASE_URLS[link.id] === currentOrigin;
          return (
            <a
              key={link.id}
              href={`${ENV_BASE_URLS[link.id]}/${link.id}`}
              className={`flex items-center gap-2 rounded-sm px-2.5 py-1.5 text-sm font-medium ${
                isActive ? "bg-accent/10 text-accent" : "text-text-muted hover:text-text"
              }`}
            >
              <span className={`h-2 w-2 shrink-0 rounded-full ${dotToneFor(link.id)}`} aria-hidden="true" />
              {link.label}
            </a>
          );
        })}
      </nav>

      <div className="my-3 border-t border-border" />

      <nav className="flex flex-col gap-1">
        {SECONDARY_LINKS.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            className={({ isActive }) =>
              `rounded-sm px-2.5 py-1.5 text-sm font-medium ${
                isActive ? "bg-accent/10 text-accent" : "text-text-muted hover:text-text"
              }`
            }
          >
            {link.label}
          </NavLink>
        ))}
      </nav>

      <div className="mt-auto pt-4">
        <ThemeToggle />
      </div>
    </aside>
  );
}
