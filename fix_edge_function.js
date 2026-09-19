const fs = require('fs');
let ts = fs.readFileSync('supabase/functions/admin-publish-product/index.ts', 'utf8');
ts = ts.replace(/    const \{ draft_id, updated_data \} = await req\.json\(\);\n    if \(\!draft_id \|\| \!updated_data\) \{\n      return new Response\(JSON\.stringify\(\{ error: "Missing draft_id or updated_data" \}\), \{ status: 400, headers: corsHeaders \}\);\n    \}\n/m, "");
fs.writeFileSync('supabase/functions/admin-publish-product/index.ts', ts);
console.log("Fixed duplicate declaration");
