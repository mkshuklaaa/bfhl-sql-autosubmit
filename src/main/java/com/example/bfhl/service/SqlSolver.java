package com.example.bfhl.service;

import org.springframework.stereotype.Service;

/**
 * Holds the final SQL strings we will submit.
 * Replace if your assignment text specifies a different schema.
 */
@Service
public class SqlSolver {

  /**
   * QUESTION 1 (odd last two digits):
   * "Return each customer's latest order (by order_date),
   * including customer_id, order_id, order_date, and total_amount.
   * If two orders tie on date, pick the one with higher total_amount."
   *
   * Works on typical schemas: customers(id), orders(id, customer_id, order_date, total_amount).
   */
  public String sqlForQuestion1() {
    return """
      SELECT o.customer_id,
             o.id          AS order_id,
             o.order_date,
             o.total_amount
      FROM orders o
      JOIN (
          SELECT customer_id,
                 MAX(order_date) AS max_date
          FROM orders
          GROUP BY customer_id
      ) m ON m.customer_id = o.customer_id
         AND m.max_date     = o.order_date
      QUALIFY ROW_NUMBER() OVER (
        PARTITION BY o.customer_id
        ORDER BY o.order_date DESC, o.total_amount DESC
      ) = 1
      """;
    /*
      Note: If your DB doesn't support QUALIFY (e.g., MySQL/Postgres),
      swap to a window + subquery:

      SELECT customer_id, order_id, order_date, total_amount
      FROM (
        SELECT o.customer_id,
               o.id AS order_id,
               o.order_date,
               o.total_amount,
               ROW_NUMBER() OVER (
                 PARTITION BY o.customer_id
                 ORDER BY o.order_date DESC, o.total_amount DESC
               ) AS rn
        FROM orders o
      ) t
      WHERE rn = 1;
    */
  }

  /**
   * QUESTION 2 (even last two digits):
   * "Find products that EVERY customer purchased at least once in 2024,
   * returning product_id and product_name. Consider paid (completed) orders only."
   *
   * Typical tables: products(id, name), orders(id, customer_id, status, order_date),
   * order_items(order_id, product_id, qty).
   */
  public String sqlForQuestion2() {
    return """
      WITH universe AS (
        SELECT COUNT(DISTINCT o.customer_id) AS total_customers
        FROM orders o
        WHERE o.status = 'COMPLETED'
          AND o.order_date >= DATE '2024-01-01'
          AND o.order_date <  DATE '2025-01-01'
      ),
      per_product AS (
        SELECT oi.product_id,
               COUNT(DISTINCT o.customer_id) AS buyers
        FROM orders o
        JOIN order_items oi ON oi.order_id = o.id
        WHERE o.status = 'COMPLETED'
          AND o.order_date >= DATE '2024-01-01'
          AND o.order_date <  DATE '2025-01-01'
        GROUP BY oi.product_id
      )
      SELECT p.id   AS product_id,
             p.name AS product_name
      FROM per_product pp
      JOIN universe u ON 1=1
      JOIN products p ON p.id = pp.product_id
      WHERE pp.buyers = u.total_customers
      ORDER BY p.id
      """;
  }
}
