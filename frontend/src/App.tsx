import { Navigate, Route, Routes } from "react-router-dom";
import { Sidebar } from "./components/Sidebar";
import { EnvironmentPage } from "./pages/EnvironmentPage";
import { ComparePage } from "./pages/ComparePage";
import { HistoryPage } from "./pages/HistoryPage";
import { HistoryDetailPage } from "./pages/HistoryDetailPage";

export default function App() {
  return (
    <div className="flex min-h-screen">
      <Sidebar />
      <main className="min-w-0 flex-1 px-4 py-6 sm:px-8 sm:py-8">
        <div className="mx-auto flex max-w-5xl flex-col">
          <Routes>
            <Route path="/" element={<Navigate to="/dev" replace />} />
            <Route path="/dev" element={<EnvironmentPage env="dev" />} />
            <Route path="/test" element={<EnvironmentPage env="test" />} />
            <Route path="/prod" element={<EnvironmentPage env="prod" />} />
            <Route path="/compare" element={<ComparePage />} />
            <Route path="/history" element={<HistoryPage />} />
            <Route path="/history/:commitSha" element={<HistoryDetailPage />} />
          </Routes>
        </div>
      </main>
    </div>
  );
}
