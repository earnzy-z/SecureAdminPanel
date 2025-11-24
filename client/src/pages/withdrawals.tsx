import { useState } from "react";
import { useQuery, useMutation } from "@tanstack/react-query";
import { Withdrawal } from "@shared/schema";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { useToast } from "@/hooks/use-toast";
import { apiRequest, queryClient } from "@/lib/queryClient";
import { DollarSign, Check, X, Clock } from "lucide-react";

export default function Withdrawals() {
  const [statusFilter, setStatusFilter] = useState("all");
  const [selectedWithdrawal, setSelectedWithdrawal] = useState<Withdrawal | null>(null);
  const [adminNote, setAdminNote] = useState("");
  const { toast } = useToast();

  const { data: withdrawals, isLoading } = useQuery<Withdrawal[]>({
    queryKey: ["/api/withdrawals"],
  });

  const processWithdrawalMutation = useMutation({
    mutationFn: async ({ id, status, note }: { id: string; status: string; note?: string }) => {
      return await apiRequest("POST", `/api/withdrawals/${id}/process`, { status, adminNote: note });
    },
    onSuccess: (_, { status }) => {
      queryClient.invalidateQueries({ queryKey: ["/api/withdrawals"] });
      queryClient.invalidateQueries({ queryKey: ["/api/users"] });
      setSelectedWithdrawal(null);
      setAdminNote("");
      toast({
        title: status === "approved" ? "Withdrawal Approved" : "Withdrawal Rejected",
        description: `The withdrawal request has been ${status}.`,
      });
    },
  });

  const filteredWithdrawals = withdrawals?.filter((w) => 
    statusFilter === "all" || w.status === statusFilter
  );

  const getStatusBadge = (status: string) => {
    const variants: Record<string, { variant: "default" | "secondary" | "destructive"; color?: string }> = {
      pending: { variant: "secondary" },
      approved: { variant: "default", color: "bg-green-600 hover:bg-green-700" },
      rejected: { variant: "destructive" },
      processing: { variant: "secondary", color: "bg-blue-600 hover:bg-blue-700" },
      completed: { variant: "default", color: "bg-green-600 hover:bg-green-700" },
    };

    const config = variants[status] || { variant: "secondary" as const };
    return (
      <Badge variant={config.variant} className={config.color}>
        {status.charAt(0).toUpperCase() + status.slice(1)}
      </Badge>
    );
  };

  const handleApprove = () => {
    if (selectedWithdrawal) {
      processWithdrawalMutation.mutate({
        id: selectedWithdrawal.id,
        status: "approved",
        note: adminNote,
      });
    }
  };

  const handleReject = () => {
    if (selectedWithdrawal) {
      processWithdrawalMutation.mutate({
        id: selectedWithdrawal.id,
        status: "rejected",
        note: adminNote,
      });
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-foreground">Withdrawal Requests</h1>
          <p className="text-sm text-muted-foreground">Review and process user withdrawal requests</p>
        </div>
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-full md:w-48" data-testid="select-status-filter">
            <SelectValue placeholder="Filter by status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Status</SelectItem>
            <SelectItem value="pending">Pending</SelectItem>
            <SelectItem value="approved">Approved</SelectItem>
            <SelectItem value="rejected">Rejected</SelectItem>
            <SelectItem value="processing">Processing</SelectItem>
            <SelectItem value="completed">Completed</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <Card>
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
                    <TableHead>User ID</TableHead>
                    <TableHead className="text-right">Amount</TableHead>
                    <TableHead>Method</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Requested</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredWithdrawals?.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} className="text-center py-12 text-muted-foreground">
                        No withdrawal requests found
                      </TableCell>
                    </TableRow>
                  ) : (
                    filteredWithdrawals?.map((withdrawal) => (
                      <TableRow key={withdrawal.id} data-testid={`row-withdrawal-${withdrawal.id}`}>
                        <TableCell className="font-mono text-sm">{withdrawal.userId.slice(0, 12)}...</TableCell>
                        <TableCell className="text-right font-mono font-semibold">${withdrawal.amount.toLocaleString()}</TableCell>
                        <TableCell>
                          <Badge variant="outline">{withdrawal.method}</Badge>
                        </TableCell>
                        <TableCell>{getStatusBadge(withdrawal.status)}</TableCell>
                        <TableCell className="text-sm text-muted-foreground">
                          {new Date(withdrawal.createdAt).toLocaleString()}
                        </TableCell>
                        <TableCell className="text-right">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => setSelectedWithdrawal(withdrawal)}
                            data-testid={`button-view-${withdrawal.id}`}
                          >
                            View Details
                          </Button>
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

      <Dialog open={!!selectedWithdrawal} onOpenChange={(open) => !open && setSelectedWithdrawal(null)}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Withdrawal Request Details</DialogTitle>
            <DialogDescription>Review and process this withdrawal request</DialogDescription>
          </DialogHeader>
          {selectedWithdrawal && (
            <div className="space-y-6">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">User ID</p>
                  <p className="text-base font-mono font-semibold mt-1">{selectedWithdrawal.userId}</p>
                </div>
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Status</p>
                  <div className="mt-1">{getStatusBadge(selectedWithdrawal.status)}</div>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Amount</p>
                  <p className="text-2xl font-bold mt-1">${selectedWithdrawal.amount.toLocaleString()}</p>
                </div>
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Payment Method</p>
                  <Badge variant="outline" className="mt-1">{selectedWithdrawal.method}</Badge>
                </div>
              </div>

              <div>
                <p className="text-sm font-medium text-muted-foreground mb-2">Account Details</p>
                <div className="p-3 rounded-md bg-muted font-mono text-sm">
                  {selectedWithdrawal.accountDetails}
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <p className="text-muted-foreground">Requested At</p>
                  <p className="font-medium mt-1">{new Date(selectedWithdrawal.createdAt).toLocaleString()}</p>
                </div>
                {selectedWithdrawal.processedAt && (
                  <div>
                    <p className="text-muted-foreground">Processed At</p>
                    <p className="font-medium mt-1">{new Date(selectedWithdrawal.processedAt).toLocaleString()}</p>
                  </div>
                )}
              </div>

              {selectedWithdrawal.adminNote && (
                <div>
                  <p className="text-sm font-medium text-muted-foreground mb-2">Admin Note</p>
                  <p className="text-sm p-3 rounded-md bg-muted">{selectedWithdrawal.adminNote}</p>
                </div>
              )}

              {selectedWithdrawal.status === "pending" && (
                <div className="space-y-4 pt-4 border-t">
                  <div>
                    <Label htmlFor="admin-note">Admin Note (optional)</Label>
                    <Textarea
                      id="admin-note"
                      placeholder="Add a note for this withdrawal..."
                      value={adminNote}
                      onChange={(e) => setAdminNote(e.target.value)}
                      className="mt-2"
                      data-testid="input-admin-note"
                    />
                  </div>
                  <div className="flex gap-2">
                    <Button
                      variant="default"
                      className="flex-1 gap-2 bg-green-600 hover:bg-green-700"
                      onClick={handleApprove}
                      disabled={processWithdrawalMutation.isPending}
                      data-testid="button-approve"
                    >
                      <Check className="h-4 w-4" />
                      Approve
                    </Button>
                    <Button
                      variant="destructive"
                      className="flex-1 gap-2"
                      onClick={handleReject}
                      disabled={processWithdrawalMutation.isPending}
                      data-testid="button-reject"
                    >
                      <X className="h-4 w-4" />
                      Reject
                    </Button>
                  </div>
                </div>
              )}
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
