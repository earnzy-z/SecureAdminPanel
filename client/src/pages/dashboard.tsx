import { useQuery } from "@tanstack/react-query";
import { Users, CreditCard, Wallet, DollarSign, TrendingUp, TrendingDown } from "lucide-react";
import { StatCard } from "@/components/stat-card";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { BarChart, Bar, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from "recharts";

export default function Dashboard() {
  const { data: stats, isLoading } = useQuery({
    queryKey: ["/api/stats"],
  });

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {[1, 2, 3, 4].map((i) => (
            <Card key={i}>
              <CardHeader>
                <Skeleton className="h-4 w-24" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-8 w-32" />
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  const dailyEarnings = [
    { day: "Mon", amount: 2400 },
    { day: "Tue", amount: 1398 },
    { day: "Wed", amount: 9800 },
    { day: "Thu", amount: 3908 },
    { day: "Fri", amount: 4800 },
    { day: "Sat", amount: 3800 },
    { day: "Sun", amount: 4300 },
  ];

  const userGrowth = [
    { month: "Jan", users: 400 },
    { month: "Feb", users: 600 },
    { month: "Mar", users: 800 },
    { month: "Apr", users: 1200 },
    { month: "May", users: 1600 },
    { month: "Jun", users: 2100 },
  ];

  const userStatus = [
    { name: "Active", value: stats?.activeUsers || 750, color: "#22c55e" },
    { name: "Inactive", value: stats?.inactiveUsers || 250, color: "#94a3b8" },
    { name: "Banned", value: stats?.bannedUsers || 50, color: "#ef4444" },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-foreground">Dashboard</h1>
        <p className="text-sm text-muted-foreground">Overview of your earning app platform</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatCard
          title="Total Users"
          value={stats?.totalUsers || 1050}
          icon={Users}
          trend={{ value: "12% this week", positive: true }}
        />
        <StatCard
          title="Total Transactions"
          value={stats?.totalTransactions || 8420}
          icon={CreditCard}
          trend={{ value: "8% this week", positive: true }}
        />
        <StatCard
          title="Total Coins Distributed"
          value={(stats?.totalCoins || 245000).toLocaleString()}
          icon={Wallet}
          description="All time"
        />
        <StatCard
          title="Pending Withdrawals"
          value={stats?.pendingWithdrawals || 23}
          icon={DollarSign}
          trend={{ value: "3% from yesterday", positive: false }}
        />
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Daily Earnings Distribution</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={dailyEarnings}>
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                <XAxis dataKey="day" stroke="hsl(var(--muted-foreground))" />
                <YAxis stroke="hsl(var(--muted-foreground))" />
                <Tooltip
                  contentStyle={{
                    backgroundColor: "hsl(var(--popover))",
                    border: "1px solid hsl(var(--border))",
                    borderRadius: "6px",
                  }}
                />
                <Bar dataKey="amount" fill="hsl(var(--primary))" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>User Growth</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={userGrowth}>
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                <XAxis dataKey="month" stroke="hsl(var(--muted-foreground))" />
                <YAxis stroke="hsl(var(--muted-foreground))" />
                <Tooltip
                  contentStyle={{
                    backgroundColor: "hsl(var(--popover))",
                    border: "1px solid hsl(var(--border))",
                    borderRadius: "6px",
                  }}
                />
                <Line type="monotone" dataKey="users" stroke="hsl(var(--primary))" strokeWidth={2} dot={{ fill: "hsl(var(--primary))" }} />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-6 md:grid-cols-3">
        <Card>
          <CardHeader>
            <CardTitle>User Status Distribution</CardTitle>
          </CardHeader>
          <CardContent className="flex justify-center">
            <ResponsiveContainer width="100%" height={250}>
              <PieChart>
                <Pie
                  data={userStatus}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={80}
                  paddingAngle={5}
                  dataKey="value"
                >
                  {userStatus.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card className="md:col-span-2">
          <CardHeader>
            <CardTitle>Quick Stats</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center justify-between p-3 rounded-md bg-muted/50">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
                  <TrendingUp className="w-5 h-5 text-primary" />
                </div>
                <div>
                  <p className="text-sm font-medium">Today's Sign-ups</p>
                  <p className="text-xs text-muted-foreground">New user registrations</p>
                </div>
              </div>
              <span className="text-lg font-semibold">{stats?.todaySignups || 42}</span>
            </div>

            <div className="flex items-center justify-between p-3 rounded-md bg-muted/50">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-chart-2/10 flex items-center justify-center">
                  <Wallet className="w-5 h-5" style={{ color: "hsl(var(--chart-2))" }} />
                </div>
                <div>
                  <p className="text-sm font-medium">Coins Earned Today</p>
                  <p className="text-xs text-muted-foreground">Total platform activity</p>
                </div>
              </div>
              <span className="text-lg font-semibold">{stats?.todayCoins || 12450}</span>
            </div>

            <div className="flex items-center justify-between p-3 rounded-md bg-muted/50">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-destructive/10 flex items-center justify-center">
                  <TrendingDown className="w-5 h-5 text-destructive" />
                </div>
                <div>
                  <p className="text-sm font-medium">Active Support Tickets</p>
                  <p className="text-xs text-muted-foreground">Requires attention</p>
                </div>
              </div>
              <span className="text-lg font-semibold">{stats?.activeTickets || 7}</span>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
