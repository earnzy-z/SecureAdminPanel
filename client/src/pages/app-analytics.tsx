import { useQuery } from "@tanstack/react-query";
import { Card } from "@/components/ui/card";
import { BarChart, LineChart } from "@/components/charts";

export default function AppAnalytics() {
  const { data: stats } = useQuery({
    queryKey: ["/api/stats"],
    queryFn: () => fetch("/api/stats").then(r => r.json()),
  });

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold">Platform Analytics</h2>

      {/* Key Metrics */}
      <div className="grid grid-cols-4 gap-4">
        <Card className="p-4">
          <p className="text-sm text-gray-500">Active Users</p>
          <p className="text-3xl font-bold">{stats?.activeUsers || 0}</p>
          <p className="text-xs text-green-600 mt-1">+5% this week</p>
        </Card>
        <Card className="p-4">
          <p className="text-sm text-gray-500">Total Coins Distributed</p>
          <p className="text-3xl font-bold">{(stats?.totalCoins || 0) / 1000}K</p>
          <p className="text-xs text-green-600 mt-1">+12% this week</p>
        </Card>
        <Card className="p-4">
          <p className="text-sm text-gray-500">Daily Signups</p>
          <p className="text-3xl font-bold">{stats?.todaySignups || 0}</p>
          <p className="text-xs text-green-600 mt-1">+8% this week</p>
        </Card>
        <Card className="p-4">
          <p className="text-sm text-gray-500">Pending Withdrawals</p>
          <p className="text-3xl font-bold">{stats?.pendingWithdrawals || 0}</p>
          <p className="text-xs text-orange-600 mt-1">Needs action</p>
        </Card>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-2 gap-4">
        <Card className="p-4">
          <h3 className="font-semibold mb-4">Daily Active Users</h3>
          <div className="h-64 bg-gray-100 rounded flex items-center justify-center text-gray-500">
            Chart: Daily user activity
          </div>
        </Card>
        <Card className="p-4">
          <h3 className="font-semibold mb-4">Coin Distribution</h3>
          <div className="h-64 bg-gray-100 rounded flex items-center justify-center text-gray-500">
            Chart: Coin earning patterns
          </div>
        </Card>
      </div>

      {/* Detailed Stats */}
      <Card className="p-4">
        <h3 className="font-semibold mb-4">User Breakdown</h3>
        <div className="space-y-2">
          <div className="flex justify-between items-center p-2 border-b">
            <span>Total Users</span>
            <span className="font-bold">{stats?.totalUsers || 0}</span>
          </div>
          <div className="flex justify-between items-center p-2 border-b">
            <span>Banned Users</span>
            <span className="font-bold text-red-600">{stats?.bannedUsers || 0}</span>
          </div>
          <div className="flex justify-between items-center p-2 border-b">
            <span>Active Tasks Completed</span>
            <span className="font-bold">{stats?.totalTransactions || 0}</span>
          </div>
          <div className="flex justify-between items-center p-2">
            <span>Support Tickets</span>
            <span className="font-bold">{stats?.activeTickets || 0}</span>
          </div>
        </div>
      </Card>
    </div>
  );
}
