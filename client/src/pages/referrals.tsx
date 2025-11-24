import { useQuery } from "@tanstack/react-query";
import { Referral } from "@shared/schema";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { StatCard } from "@/components/stat-card";
import { Users, UserPlus, TrendingUp, DollarSign } from "lucide-react";

export default function Referrals() {
  const { data: referrals, isLoading } = useQuery<Referral[]>({
    queryKey: ["/api/referrals"],
  });

  const stats = {
    totalReferrals: referrals?.length || 0,
    activeReferrals: referrals?.filter(r => r.status === "active").length || 0,
    totalCoinsEarned: referrals?.reduce((sum, r) => sum + r.coinsEarned, 0) || 0,
    avgCoinsPerReferral: referrals?.length
      ? Math.round((referrals.reduce((sum, r) => sum + r.coinsEarned, 0) / referrals.length))
      : 0,
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-foreground">Referral System</h1>
        <p className="text-sm text-muted-foreground">Track and analyze user referrals</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatCard
          title="Total Referrals"
          value={stats.totalReferrals}
          icon={UserPlus}
          description="All time"
        />
        <StatCard
          title="Active Referrals"
          value={stats.activeReferrals}
          icon={Users}
          description="Currently active"
        />
        <StatCard
          title="Total Coins Distributed"
          value={stats.totalCoinsEarned.toLocaleString()}
          icon={DollarSign}
          description="From referrals"
        />
        <StatCard
          title="Avg Coins/Referral"
          value={stats.avgCoinsPerReferral}
          icon={TrendingUp}
          description="Average earnings"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Referral Activity</CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          {isLoading ? (
            <div className="p-6 space-y-4">
              {[1, 2, 3, 4, 5].map((i) => (
                <Skeleton key={i} className="h-12 w-full" />
              ))}
            </div>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Referrer ID</TableHead>
                    <TableHead>Referred ID</TableHead>
                    <TableHead className="text-right">Coins Earned</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Date</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {referrals?.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5} className="text-center py-12 text-muted-foreground">
                        No referral data available
                      </TableCell>
                    </TableRow>
                  ) : (
                    referrals?.map((referral) => (
                      <TableRow key={referral.id} data-testid={`row-referral-${referral.id}`}>
                        <TableCell className="font-mono text-sm">
                          {referral.referrerId.slice(0, 12)}...
                        </TableCell>
                        <TableCell className="font-mono text-sm">
                          {referral.referredId.slice(0, 12)}...
                        </TableCell>
                        <TableCell className="text-right font-mono font-semibold">
                          {referral.coinsEarned.toLocaleString()}
                        </TableCell>
                        <TableCell>
                          {referral.status === "active" ? (
                            <Badge className="bg-green-600 hover:bg-green-700">Active</Badge>
                          ) : (
                            <Badge variant="secondary">Inactive</Badge>
                          )}
                        </TableCell>
                        <TableCell className="text-sm text-muted-foreground">
                          {new Date(referral.createdAt).toLocaleDateString()}
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
