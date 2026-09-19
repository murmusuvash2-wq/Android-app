const fs = require('fs');
let js = fs.readFileSync('admin-panel/app.js', 'utf8');

const replacement1 = `Query Error: \${adminErr ? adminErr.message + ' (' + adminErr.code + ')' : 'No matching row found in public.admins.'}
                        </span>
                        
                        <div class="mt-6 p-4 bg-white border border-stone-200 rounded-lg">
                            <h3 class="font-bold text-sm text-stone-900 mb-2">[DEV ONLY] Edge Function Security Test</h3>
                            <p class="text-xs text-stone-500 mb-4">Click to simulate a direct API attack attempting to publish a product as this unauthorized user.</p>
                            <button id="security-test-btn" class="bg-red-600 text-white px-4 py-2 rounded text-sm hover:bg-red-700 font-medium">Invoke admin-publish-product</button>
                            <pre id="security-test-result" class="mt-4 text-xs font-mono bg-stone-900 text-stone-100 p-3 rounded hidden whitespace-pre-wrap"></pre>
                        </div>`;

js = js.replace(/Query Error: \$\{adminErr \? adminErr\.message \+ ' \(' \+ adminErr\.code \+ '\)' : 'No matching row found in public\.admins\.'\}\n\s*<\/span>/, replacement1);

const replacement2 = `workspaceSec.classList.remove('hidden');
                    
                    // Attach Security Test listener
                    setTimeout(() => {
                        const btn = document.getElementById('security-test-btn');
                        if (btn) {
                            btn.addEventListener('click', async () => {
                                const out = document.getElementById('security-test-result');
                                out.classList.remove('hidden');
                                out.textContent = "Invoking Edge Function as non-admin...";
                                
                                try {
                                    const { data, error } = await window.supabaseClient.functions.invoke('admin-publish-product', { 
                                        body: { draft_id: 'fake-draft-123', updated_data: {} } 
                                    });
                                    
                                    out.textContent = JSON.stringify({
                                        returnedData: data,
                                        returnedError: error
                                    }, null, 2);
                                } catch (e) {
                                    out.textContent = "Exception: " + e.message;
                                }
                            });
                        }
                    }, 50);
                }`;

js = js.replace(/workspaceSec\.classList\.remove\('hidden'\);\n\s*\}/, replacement2 + '\n                }');

fs.writeFileSync('admin-panel/app.js', js);
console.log("Patched Security Test button into Access Denied screen.");
