import { useQuery } from "@tanstack/react-query";
import { fetchConfigInfo } from "../lib/api";

export function useConfigInfo() {
  return useQuery({
    queryKey: ["config-info"],
    queryFn: fetchConfigInfo,
    refetchInterval: 15000,
    staleTime: 10000,
  });
}
