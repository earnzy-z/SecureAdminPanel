import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Lock, LogIn } from "lucide-react";

export default function Login() {
  const handleLogin = () => {
    window.location.href = "/api/login";
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-gradient-to-br from-background via-background to-muted p-4">
      <div className="w-full max-w-md">
        <Card className="border shadow-lg">
          <CardHeader className="space-y-4 text-center">
            <div className="flex justify-center">
              <div className="w-16 h-16 rounded-full bg-primary/10 flex items-center justify-center">
                <Lock className="w-8 h-8 text-primary" />
              </div>
            </div>
            <div>
              <CardTitle className="text-2xl">Admin Panel</CardTitle>
              <CardDescription>Secure access to platform management</CardDescription>
            </div>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="space-y-2 text-center">
              <p className="text-sm text-muted-foreground">
                Sign in with your Replit account to access the admin dashboard
              </p>
            </div>

            <Button
              onClick={handleLogin}
              size="lg"
              className="w-full gap-2"
              data-testid="button-login"
            >
              <LogIn className="h-4 w-4" />
              Sign In with Replit
            </Button>

            <div className="space-y-2 pt-4 border-t">
              <h3 className="text-sm font-semibold">What you can do:</h3>
              <ul className="text-xs text-muted-foreground space-y-1">
                <li>✓ Manage users and their accounts</li>
                <li>✓ Control coins and rewards</li>
                <li>✓ Monitor transactions and withdrawals</li>
                <li>✓ Create and manage offers</li>
                <li>✓ Send push notifications</li>
                <li>✓ Track analytics and leaderboards</li>
              </ul>
            </div>
          </CardContent>
        </Card>

        <p className="text-center text-xs text-muted-foreground mt-6">
          Only authorized administrators can access this panel
        </p>
      </div>
    </div>
  );
}
