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
    retry: false,
  });

  return {
    user: user as AdminUser | undefined,
    isLoading,
    isAuthenticated: !!user,
  };
}
