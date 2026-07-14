import type { Metadata } from "next";
import "./styles.css";

export const metadata: Metadata = {
  title: "BudgetScope",
  description: "Household cash-flow visibility and recurring payment planning.",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
