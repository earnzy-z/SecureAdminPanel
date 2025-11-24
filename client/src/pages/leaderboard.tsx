import { useQuery } from "@tanstack/react-query";
import { Leaderboard as LeaderboardType } from "@shared/schema";
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
import { Trophy, Medal, Award } from "lucide-react";

export default function Leaderboard() {
  const { data: leaderboard, isLoading } = useQuery<LeaderboardType[]>({
    queryKey: ["/api/leaderboard"],
  });

  const getRankIcon = (rank: number) => {
    if (rank === 1) return <Trophy className="h-5 w-5 text-yellow-500" />;
    if (rank === 2) return <Medal className="h-5 w-5 text-gray-400" />;
    if (rank === 3) return <Award className="h-5 w-5 text-amber-700" />;
    return null;
  };

  const getRankBadge = (rank: number) => {
    if (rank === 1) return <Badge className="bg-yellow-600 hover:bg-yellow-700">1st</Badge>;
    if (rank === 2) return <Badge className="bg-gray-500 hover:bg-gray-600">2nd</Badge>;
    if (rank === 3) return <Badge className="bg-amber-700 hover:bg-amber-800">3rd</Badge>;
    return <span className="text-lg font-semibold text-muted-foreground">#{rank}</span>;
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-foreground">Leaderboard</h1>
        <p className="text-sm text-muted-foreground">Top earning users on the platform</p>
      </div>

      <div className="grid gap-6 md:grid-cols-3">
        {isLoading ? (
          <>
            {[1, 2, 3].map((i) => (
              <Skeleton key={i} className="h-32" />
            ))}
          </>
        ) : (
          leaderboard?.slice(0, 3).map((entry, idx) => (
            <Card key={entry.id} className={idx === 0 ? "border-yellow-500/50" : ""}>
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <CardTitle className="text-base">Rank {idx + 1}</CardTitle>
                  {getRankIcon(idx + 1)}
                </div>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground mb-2">User ID: {entry.userId.slice(0, 12)}...</p>
                <div className="flex items-baseline gap-1">
                  <span className="text-3xl font-bold">{entry.totalCoins.toLocaleString()}</span>
                  <span className="text-sm text-muted-foreground">coins</span>
                </div>
              </CardContent>
            </Card>
          ))
        )}
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Full Rankings</CardTitle>
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
                    <TableHead className="w-24">Rank</TableHead>
                    <TableHead>User ID</TableHead>
                    <TableHead className="text-right">Total Coins</TableHead>
                    <TableHead>Last Updated</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {leaderboard?.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={4} className="text-center py-12 text-muted-foreground">
                        No leaderboard data available
                      </TableCell>
                    </TableRow>
                  ) : (
                    leaderboard?.map((entry) => (
                      <TableRow key={entry.id} data-testid={`row-leaderboard-${entry.rank}`}>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            {getRankIcon(entry.rank)}
                            {getRankBadge(entry.rank)}
                          </div>
                        </TableCell>
                        <TableCell className="font-mono">{entry.userId}</TableCell>
                        <TableCell className="text-right font-mono font-semibold text-lg">
                          {entry.totalCoins.toLocaleString()}
                        </TableCell>
                        <TableCell className="text-sm text-muted-foreground">
                          {new Date(entry.updatedAt).toLocaleString()}
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
