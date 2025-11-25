import { Switch, Route } from "wouter";
import { queryClient } from "./lib/queryClient";
import { QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import NotFound from "@/pages/not-found";
import { SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/app-sidebar";
import ThemeToggle from "@/components/theme-toggle";

// Import all pages
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
import SupportTickets from "@/pages/support-tickets";
import SupportLiveChat from "@/pages/support-live-chat";
import Leaderboard from "@/pages/leaderboard";
import Referrals from "@/pages/referrals";
import Withdrawals from "@/pages/withdrawals";
import AutoBanRules from "@/pages/auto-ban";
import Settings from "@/pages/settings";
import AppAnalytics from "@/pages/app-analytics";

function Router() {
  return (
    <Switch>
      <Route path="/" component={Dashboard} />
      <Route path="/users" component={Users} />
      <Route path="/transactions" component={Transactions} />
      <Route path="/coins" component={CoinControl} />
      <Route path="/offers" component={Offers} />
      <Route path="/banners" component={Banners} />
      <Route path="/tasks" component={Tasks} />
      <Route path="/achievements" component={Achievements} />
      <Route path="/notifications" component={Notifications} />
      <Route path="/promo-codes" component={PromoCodes} />
      <Route path="/support/tickets" component={SupportTickets} />
      <Route path="/support/:id" component={SupportLiveChat} />
      <Route path="/leaderboard" component={Leaderboard} />
      <Route path="/referrals" component={Referrals} />
      <Route path="/withdrawals" component={Withdrawals} />
      <Route path="/auto-ban-rules" component={AutoBanRules} />
      <Route path="/settings" component={Settings} />
      <Route path="/analytics" component={AppAnalytics} />
      <Route component={NotFound} />
    </Switch>
  );
}

export default function App() {
  const style = {
    "--sidebar-width": "16rem",
    "--sidebar-width-icon": "3rem",
  } as React.CSSProperties;

  return (
    <QueryClientProvider client={queryClient}>
      <TooltipProvider>
        <SidebarProvider style={style}>
          <div className="flex h-screen w-full">
            <AppSidebar />
            <div className="flex flex-col flex-1">
              <header className="flex items-center justify-between p-4 border-b">
                <SidebarTrigger data-testid="button-sidebar-toggle" />
                <ThemeToggle />
              </header>
              <main className="flex-1 overflow-auto">
                <Router />
              </main>
            </div>
          </div>
        </SidebarProvider>
        <Toaster />
      </TooltipProvider>
    </QueryClientProvider>
  );
}
