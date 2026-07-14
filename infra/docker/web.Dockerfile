FROM node:22-alpine AS development

WORKDIR /workspace/budget-scope/apps/web
RUN corepack enable
COPY apps/web/package.json ./
RUN pnpm install

COPY apps/web ./
EXPOSE 3000
CMD ["pnpm", "dev", "--", "--hostname", "0.0.0.0"]
