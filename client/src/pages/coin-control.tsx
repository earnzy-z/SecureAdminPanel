import { useState } from "react";
import { useQuery, useMutation } from "@tanstack/react-query";
import { User } from "@shared/schema";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useToast } from "@/hooks/use-toast";
import { apiRequest, queryClient } from "@/lib/queryClient";
import { Wallet, Plus, Minus, Users } from "lucide-react";

export default function CoinControl() {
  const [selectedUserId, setSelectedUserId] = useState("");
  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [operation, setOperation] = useState<"add" | "subtract">("add");
  const { toast } = useToast();

  const { data: users } = useQuery<User[]>({
    queryKey: ["/api/users"],
  });

  const adjustCoinsMutation = useMutation({
    mutationFn: async (data: { userId: string; amount: number; description: string }) => {
      return await apiRequest("POST", "/api/coins/adjust", data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["/api/users"] });
      queryClient.invalidateQueries({ queryKey: ["/api/transactions"] });
      setAmount("");
      setDescription("");
      toast({
        title: "Coins Adjusted",
        description: "User balance has been updated successfully.",
      });
    },
  });

  const bulkCreditMutation = useMutation({
    mutationFn: async (data: { amount: number; description: string }) => {
      return await apiRequest("POST", "/api/coins/bulk-credit", data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["/api/users"] });
      queryClient.invalidateQueries({ queryKey: ["/api/transactions"] });
      setAmount("");
      setDescription("");
      toast({
        title: "Bulk Credit Completed",
        description: "Coins have been credited to all users.",
      });
    },
  });

  const handleAdjustCoins = () => {
    const coinAmount = parseInt(amount);
    if (!selectedUserId || isNaN(coinAmount) || coinAmount <= 0) {
      toast({
        title: "Invalid Input",
        description: "Please select a user and enter a valid amount.",
        variant: "destructive",
      });
      return;
    }

    adjustCoinsMutation.mutate({
      userId: selectedUserId,
      amount: operation === "add" ? coinAmount : -coinAmount,
      description: description || `Manual ${operation === "add" ? "credit" : "debit"} by admin`,
    });
  };

  const handleBulkCredit = () => {
    const coinAmount = parseInt(amount);
    if (isNaN(coinAmount) || coinAmount <= 0) {
      toast({
        title: "Invalid Input",
        description: "Please enter a valid amount.",
        variant: "destructive",
      });
      return;
    }

    bulkCreditMutation.mutate({
      amount: coinAmount,
      description: description || "Bulk credit by admin",
    });
  };

  const selectedUser = users?.find(u => u.id === selectedUserId);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-foreground">Coin Control</h1>
        <p className="text-sm text-muted-foreground">Manage user coin balances and bulk operations</p>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Wallet className="h-5 w-5" />
              Adjust User Coins
            </CardTitle>
            <CardDescription>Add or subtract coins from a specific user</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="user-select">Select User</Label>
              <Select value={selectedUserId} onValueChange={setSelectedUserId}>
                <SelectTrigger id="user-select" data-testid="select-user">
                  <SelectValue placeholder="Choose a user" />
                </SelectTrigger>
                <SelectContent>
                  {users?.map((user) => (
                    <SelectItem key={user.id} value={user.id}>
                      {user.username} ({user.coins} coins)
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {selectedUser && (
              <div className="p-3 rounded-md bg-muted/50">
                <p className="text-sm text-muted-foreground">Current Balance</p>
                <p className="text-2xl font-bold mt-1">{selectedUser.coins.toLocaleString()} coins</p>
              </div>
            )}

            <div className="space-y-2">
              <Label htmlFor="operation">Operation</Label>
              <Select value={operation} onValueChange={(v) => setOperation(v as "add" | "subtract")}>
                <SelectTrigger id="operation" data-testid="select-operation">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="add">
                    <div className="flex items-center gap-2">
                      <Plus className="h-4 w-4" />
                      Add Coins
                    </div>
                  </SelectItem>
                  <SelectItem value="subtract">
                    <div className="flex items-center gap-2">
                      <Minus className="h-4 w-4" />
                      Subtract Coins
                    </div>
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="amount">Amount</Label>
              <Input
                id="amount"
                type="number"
                placeholder="Enter coin amount"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                data-testid="input-amount"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Description (optional)</Label>
              <Textarea
                id="description"
                placeholder="Reason for adjustment..."
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                data-testid="input-description"
              />
            </div>

            <Button
              className="w-full"
              onClick={handleAdjustCoins}
              disabled={adjustCoinsMutation.isPending}
              data-testid="button-adjust-coins"
            >
              {adjustCoinsMutation.isPending ? "Processing..." : `${operation === "add" ? "Add" : "Subtract"} Coins`}
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Users className="h-5 w-5" />
              Bulk Credit All Users
            </CardTitle>
            <CardDescription>Credit coins to all active users at once</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="p-4 rounded-md bg-blue-50 dark:bg-blue-950 border border-blue-200 dark:border-blue-900">
              <p className="text-sm font-medium text-blue-900 dark:text-blue-100">⚠️ Bulk Operation</p>
              <p className="text-xs text-blue-700 dark:text-blue-300 mt-1">
                This will credit coins to all users who are not banned. Use with caution.
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="bulk-amount">Amount per User</Label>
              <Input
                id="bulk-amount"
                type="number"
                placeholder="Enter coin amount"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                data-testid="input-bulk-amount"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="bulk-description">Description</Label>
              <Textarea
                id="bulk-description"
                placeholder="e.g., Holiday bonus, System reward..."
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                data-testid="input-bulk-description"
              />
            </div>

            <div className="p-3 rounded-md bg-muted/50">
              <p className="text-sm text-muted-foreground">Active Users</p>
              <p className="text-2xl font-bold mt-1">{users?.filter(u => !u.isBanned).length || 0}</p>
              {amount && !isNaN(parseInt(amount)) && (
                <p className="text-xs text-muted-foreground mt-2">
                  Total distribution: {((users?.filter(u => !u.isBanned).length || 0) * parseInt(amount)).toLocaleString()} coins
                </p>
              )}
            </div>

            <Button
              className="w-full"
              variant="default"
              onClick={handleBulkCredit}
              disabled={bulkCreditMutation.isPending}
              data-testid="button-bulk-credit"
            >
              {bulkCreditMutation.isPending ? "Processing..." : "Credit All Users"}
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
