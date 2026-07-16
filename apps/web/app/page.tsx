import { ApiHealthIndicator } from "./ApiHealthIndicator";

const features = [
  "Track recurring payments",
  "Understand monthly cash flow",
  "Plan budgets and savings goals",
];

export default function HomePage() {
  return (
    <main className="shell">
      <ApiHealthIndicator />
      <section className="hero" aria-labelledby="page-title">
        <p className="eyebrow">BudgetScope</p>
        <h1 id="page-title">Financial clarity for households.</h1>
        <p className="lede">
          A privacy-minded dashboard for monthly commitments, spending, budgets, and explainable
          savings insights.
        </p>
        <ul className="feature-list" aria-label="Initial capabilities">
          {features.map((feature) => (
            <li key={feature}>{feature}</li>
          ))}
        </ul>
      </section>
    </main>
  );
}
