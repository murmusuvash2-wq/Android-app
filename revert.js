const fs = require('fs');
let js = fs.readFileSync('admin-panel/app.js', 'utf8');

// We injected this block:
// <div class="mt-6 p-4 bg-white border border-stone-200 rounded-lg"> ... </div>
// and the setTimeout block. Let's surgically remove them to get exactly the last working state.

const uiToRemove = `<div class="mt-6 p-4 bg-white border border-stone-200 rounded-lg">
                            <h3 class="font-bold text-sm text-stone-900 mb-2">[DEV ONLY] Edge Function Security Test</h3>
                            <p class="text-xs text-stone-500 mb-4">Click to simulate a direct API attack attempting to publish a product as this unauthorized user.</p>
                            <button id="security-test-btn" class="bg-red-600 text-white px-4 py-2 rounded text-sm hover:bg-red-700 font-medium">Invoke admin-publish-product</button>
                            <pre id="security-test-result" class="mt-4 text-xs font-mono bg-stone-900 text-stone-100 p-3 rounded hidden whitespace-pre-wrap"></pre>
                        </div>`;

const jsToRemove = `// Attach Security Test listener
                    setTimeout(() => {
                        const btn = document.getElementById('security-test-btn');
                        if (btn) {
                            btn.addEventListener('click', async () => {
                                const out = document.getElementById('security-test-result');
                                out.classList.remove('hidden');
                                out.textContent = "Invoking Edge Function as non-admin...";
                                
                                try {
                                    const response = await supabaseClient.functions.invoke('admin-publish-product', { 
                                        body: { draft_id: 'fake-draft-123', updated_data: {} } 
                                    });
                                    
                                    out.textContent = "Result:\\n" + JSON.stringify(response, null, 2);
                                } catch (e) {
                                    out.textContent = "Exception Caught:\\n" + e.name + ": " + e.message;
                                    // if it's an HttpError, it has a context
                                    if (e.context) {
                                        out.textContent += "\\n\\nContext:\\n" + JSON.stringify(e.context, null, 2);
                                    }
                                }
                            });
                        }
                    }, 50);`;


js = js.replace(uiToRemove, '');
js = js.replace(jsToRemove, '');
js = js.replace(/\n\s*\n\s*\n/g, '\n'); // Clean up empty lines

fs.writeFileSync('admin-panel/app.js', js);
console.log("Reverted the DEV ONLY test button injections.");
