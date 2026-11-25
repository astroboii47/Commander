# Commander 0.1.0 alpha 7.1

Alpha 7.1 is a focused usability update for Commander Bar, adding lightweight widget controls and more flexible web search while refining the related Settings layout.

## Highlights

- **Widget controls in search:** selected home-screen widgets can appear as normal Commander results, with custom names and aliases. Simple button-style widgets can be activated directly from the result.
- **Three web-search modes:** choose the browser-backed Quick popup, the normal default browser, or a new inbuilt half-screen browser.
- **Inbuilt web search:** the compact browser keeps Commander Bar, its query, and the keyboard available beneath it, with back, close, normal-browser, and private-browser actions.
- **Faster inbuilt startup:** WebView is prepared only when an inbuilt web result appears, avoiding extra work during normal Commander use while reducing the wait after Enter.
- **Adaptive browser size:** the inbuilt browser expands when the keyboard is hidden and smoothly returns to its compact layout when typing resumes.
- **Cross-device sizing:** square and physical-keyboard displays use a safer browser height cap rather than the tall-phone expansion value.
- **Smoother hand-off:** launching apps, shortcuts, and browsers now uses one consistent Commander exit transition.
- Commander fades away before starting an external target, so a slow-opening app can no longer leave the Bar visibly frozen onscreen.
- **Cleaner Settings:** Hardware input, Web search, and Search results now have clearer visual grouping and spacing.

## Fixes

- Commander results no longer render over the inbuilt browser.
- The inbuilt browser no longer starts the same page load twice.
- Links navigate normally inside the inbuilt browser, and both browser hand-off buttons open the page currently being viewed rather than the original search.
- Editing or backspacing the Commander query closes the current inbuilt result and returns to updated search results.
- The experimental result-origin Quick popup animation has been removed to restore reliable live web results.

## Experimental Home status

Commander Home remains under active development and hidden in this public build. Commander Bar and Commander Hub continue to work independently.

## Privacy note

The inbuilt browser uses Android WebView and only loads after the user opens a web result. It is destroyed when the panel closes and does not add a persistent background process. Live Google query suggestions remain optional and disabled by default.
