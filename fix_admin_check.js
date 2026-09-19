const fs = require('fs');
let code = fs.readFileSync('admin-panel/app.js', 'utf8');

// The bug is `.eq('id', user.id)`. The table column is `user_id`.
// Let's replace the whole try block around the admin check with better logging and the fix.

const newCheck = `
        // --- ADMIN AUTHORIZATION QUERY ---
        try {
            console.log("Authenticated as:", user.id, "Email:", user.email);
            console.log("Checking public.admins where user_id =", user.id);
            
            const { data: adminData, error: adminErr } = await supabaseClient
                .from('admins')
                .select('user_id')
                .eq('user_id', user.id)
                .single();
                
            console.log("Admin query response:", { adminData, adminErr });
                
            if (adminErr || !adminData) {
                console.warn("Admin authorization failed:", adminErr);
                if (workspaceSec) {
                    workspaceSec.innerHTML = \`<div class="p-6 bg-red-50 text-red-700 rounded-xl border border-red-200">
                        <b>Access Denied: You do not have administrator privileges.</b><br><br>
                        <span class="text-sm font-mono opacity-80">
                        Authenticated UID: \${user.id}<br>
                        Query Error: \${adminErr ? adminErr.message + ' (' + adminErr.code + ')' : 'No matching row found in public.admins.'}
                        </span>
                    </div>\`;
                    workspaceSec.classList.remove('hidden');
                }
                return; // Stop execution
            }
        } catch (err) {
`;

code = code.replace(/\/\/ --- ADMIN AUTHORIZATION QUERY ---[\s\S]*?\} catch \(err\) \{/, newCheck);

fs.writeFileSync('admin-panel/app.js', code);
console.log("Fixed public.admins query.");
