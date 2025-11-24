import { useQuery } from "@tanstack/react-query";

export interface AdminUser {
  id: string;
  email: string;
  firstName?: string;
  lastName?: string;
  profileImageUrl?: string;
}

export function useAuth() {
  const { data: user, isLoading } = useQuery({
    queryKey: ["/api/auth/user"],
    staleTime: 1000 * 60 * 60, // 1 hour
    gcTime: 1000 * 60 * 60 * 24, // 24 hours
    retry: 1,
    retryDelay: 1000,
  });

  return {
    user: user as AdminUser | undefined,
    isLoading,
    isAuthenticated: !!user,
  };
}
