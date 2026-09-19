const fs = require('fs');
let code = fs.readFileSync('admin-panel/app.js', 'utf8');

// We will completely replace showWorkspace and loadDrafts to be safe.
code = code.replace(/function showWorkspace\(user\) \{[\s\S]*?\n\}/, `async function showWorkspace(user) {
    try {
        console.log("showWorkspace started for user", user?.email);
        const authSec = document.getElementById('auth-section');
        const workspaceSec = document.getElementById('workspace-section');
        const emailSpan = document.getElementById('user-email');
        const logout = document.getElementById('logout-btn');
        
        if (authSec) authSec.classList.add('hidden');
        if (workspaceSec) workspaceSec.classList.remove('hidden');
        if (emailSpan && user) emailSpan.textContent = user.email || 'Admin';
        if (logout) logout.classList.remove('hidden');
        
        // Wrap loadDrafts so it cannot crash this function
        loadDrafts().catch(err => console.error("loadDrafts unhandled:", err));
    } catch(err) {
        console.error("Critical error in showWorkspace:", err);
        alert("UI Error: " + err.message);
    }
}`);

code = code.replace(/async function loadDrafts\(\) \{[\s\S]*?refreshDraftsBtn\.addEventListener\('click', loadDrafts\);/, `async function loadDrafts() {
    const list = document.getElementById('drafts-list');
    if (!list) return;
    
    try {
        list.innerHTML = '<p class="text-stone-500 text-sm italic">Loading...</p>';
        const { data, error } = await supabaseClient
            .from('product_drafts')
            .select('*')
            .eq('status', 'draft')
            .order('created_at', { ascending: false });

        if (error) {
            list.innerHTML = \`<p class="text-red-500 text-sm">Error loading drafts: \${error.message}</p>\`;
            return;
        }

        if (!data || data.length === 0) {
            list.innerHTML = '<p class="text-stone-500 text-sm italic">No pending drafts.</p>';
            return;
        }

        list.innerHTML = '';
        data.forEach(draft => {
            const title = draft.extracted_data?.name || 'Unknown Product';
            const brand = draft.extracted_data?.brand || 'Unknown Brand';
            
            const el = document.createElement('div');
            el.className = 'flex justify-between items-center p-3 border border-stone-200 rounded-lg hover:bg-stone-50 transition-colors';
            el.innerHTML = \`
                <div>
                    <p class="font-medium text-sm text-stone-900">\${title}</p>
                    <p class="text-xs text-stone-500">\${brand} • \${draft.source_url}</p>
                </div>
                <button class="bg-white border border-stone-300 px-3 py-1 text-sm rounded hover:bg-stone-100 review-draft-btn">Review</button>
            \`;
            
            el.querySelector('.review-draft-btn').addEventListener('click', () => populateReviewForm(draft));
            list.appendChild(el);
        });
    } catch(err) {
        console.error("Exception in loadDrafts:", err);
        list.innerHTML = \`<p class="text-red-500 text-sm">Crash loading drafts: \${err.message}</p>\`;
    }
}
const refreshBtn = document.getElementById('refresh-drafts-btn');
if (refreshBtn) refreshBtn.addEventListener('click', loadDrafts);`);

fs.writeFileSync('admin-panel/app.js', code);
console.log("Patched showWorkspace and loadDrafts");
