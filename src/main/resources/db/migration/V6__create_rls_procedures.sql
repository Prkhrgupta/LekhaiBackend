CREATE OR REPLACE PROCEDURE create_shop_isolation_policy(
    p_table_name TEXT,
    p_policy_name TEXT DEFAULT 'shop_isolation'
)
LANGUAGE plpgsql AS $$
BEGIN
    -- Enable RLS on the table
    EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY', p_table_name);

    -- Create the policy without bypass option
    EXECUTE format('
        CREATE POLICY %I ON %I
            FOR ALL
            USING (
                shop_code = current_setting(''app.shop_code'', false)::INTEGER
                OR
                shop_code = 0
            )
            WITH CHECK (
                shop_code = current_setting(''app.shop_code'', false)::INTEGER
            )',
        p_policy_name,
        p_table_name
    );

    RAISE NOTICE 'RLS policy "%" created on table "%"', p_policy_name, p_table_name;
END;
$$;


CREATE OR REPLACE PROCEDURE create_shop_isolation_policy_with_bypass(
    p_table_name TEXT,
    p_policy_name TEXT DEFAULT 'shop_isolation'
)
LANGUAGE plpgsql AS $$
BEGIN
    -- Enable RLS on the table
    EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY', p_table_name);

    -- Create the policy with bypass option
    EXECUTE format('
        CREATE POLICY %I ON %I
            FOR ALL
            USING (
                current_setting(''app.bypass_rls'', false)::BOOLEAN = true
                OR
                shop_code = current_setting(''app.shop_code'', false)::INTEGER
                OR
                shop_code = 0
            )
            WITH CHECK (
                current_setting(''app.bypass_rls'', false)::BOOLEAN = true
                OR
                shop_code = current_setting(''app.shop_code'', false)::INTEGER
            )',
        p_policy_name,
        p_table_name
    );

    RAISE NOTICE 'RLS policy "%" created on table "%" with bypass option', p_policy_name, p_table_name;
END;
$$;