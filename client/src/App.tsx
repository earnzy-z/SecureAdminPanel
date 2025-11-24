import { Switch, Route } from "wouter";
import { queryClient } from "./lib/queryClient";
import { QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import { SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/app-sidebar";
import { ThemeProvider } from "@/components/theme-provider";
import { ThemeToggle } from "@/components/theme-toggle";
import NotFound from "@/pages/not-found";
import Dashboard from "@/pages/dashboard";
import Users from "@/pages/users";
import Transactions from "@/pages/transactions";
import CoinControl from "@/pages/coin-control";
import Offers from "@/pages/offers";
import Banners from "@/pages/banners";
import Tasks from "@/pages/tasks";
import Achievements from "@/pages/achievements";
import Notifications from "@/pages/notifications";
import PromoCodes from "@/pages/promo-codes";
import Support from "@/pages/support";
import Leaderboard from "@/pages/leaderboard";
import Referrals from "@/pages/referrals";
import Withdrawals from "@/pages/withdrawals";
import AutoBan from "@/pages/auto-ban";
import Settings from "@/pages/settings";

function Router() {
  return (
    <Switch>
      <Route path="/" component={Dashboard} />
      <Route path="/users" component={Users} />
      <Route path="/transactions" component={Transactions} />
      <Route path="/coin-control" component={CoinControl} />
      <Route path="/offers" component={Offers} />
      <Route path="/banners" component={Banners} />
      <Route path="/tasks" component={Tasks} />
      <Route path="/achievements" component={Achievements} />
      <Route path="/notifications" component={Notifications} />
      <Route path="/promo-codes" component={PromoCodes} />
      <Route path="/support" component={Support} />
      <Route path="/leaderboard" component={Leaderboard} />
      <Route path="/referrals" component={Referrals} />
      <Route path="/withdrawals" component={Withdrawals} />
      <Route path="/auto-ban" component={AutoBan} />
      <Route path="/settings" component={Settings} />
      <Route component={NotFound} />
    </Switch>
  );
}

export default function App() {
  const style = {
    "--sidebar-width": "16rem",
    "--sidebar-width-icon": "3rem",
  };

  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider defaultTheme="light" storageKey="admin-panel-theme">
        <TooltipProvider>
          <SidebarProvider style={style as React.CSSProperties}>
            <div className="flex h-screen w-full">
              <AppSidebar />
              <div className="flex flex-col flex-1 overflow-hidden">
                <header className="flex items-center justify-between p-4 border-b border-border bg-background sticky top-0 z-10">
                  <SidebarTrigger data-testid="button-sidebar-toggle" />
                  <ThemeToggle />
                </header>
                <main className="flex-1 overflow-y-auto p-6 bg-background">
                  <Router />
                </main>
              </div>
            </div>
          </SidebarProvider>
          <Toaster />
        </TooltipProvider>
      </ThemeProvider>
    </QueryClientProvider>
  );
}
