const fs = require('fs');
let js = fs.readFileSync('admin-panel/app.js', 'utf8');

// I'll rewrite the entire showWorkspace function back to exactly how it was 
// before I tried adding the security button.

const cleanShowWorkspace = `async function showWorkspace(user) {
    try {
        console.log("showWorkspace started for user", user?.email);
        const authSec = document.getElementById('auth-section');
        const workspaceSec = document.getElementById('workspace-section');
        const emailSpan = document.getElementById('user-email');
        const logout = document.getElementById('logout-btn');
        
        if (authSec) authSec.classList.add('hidden');
        if (emailSpan && user) emailSpan.textContent = user.email || 'Admin';
        if (logout) logout.classList.remove('hidden');
        
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
            console.error("Crash during admin check:", err);
            if (workspaceSec) {
                workspaceSec.innerHTML = '<div class="p-6 bg-red-50 text-red-700 rounded-xl border border-red-200">System Error during authorization check.</div>';
                workspaceSec.classList.remove('hidden');
            }
            return;
        }
        
        if (workspaceSec) workspaceSec.classList.remove('hidden');
        
        // Wrap loadDrafts so it cannot crash this function
        loadDrafts().catch(err => console.error("loadDrafts unhandled:", err));
    } catch(err) {
        console.error("Critical error in showWorkspace:", err);
        alert("UI Error: " + err.message);
    }
}`;

js = js.replace(/async function showWorkspace\(user\) \{[\s\S]*?\}\n\}\n/m, cleanShowWorkspace + '\n');
fs.writeFileSync('admin-panel/app.js', js);
console.log("Rewrote showWorkspace clean.");
