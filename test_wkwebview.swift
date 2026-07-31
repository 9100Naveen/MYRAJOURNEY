import WebKit
let url = URL(string: "https://www.youtube.com/embed/uSgBNyhXvFs?playsinline=1")!
var request = URLRequest(url: url)
request.setValue("https://www.youtube.com", forHTTPHeaderField: "Referer")
print(request.allHTTPHeaderFields)
