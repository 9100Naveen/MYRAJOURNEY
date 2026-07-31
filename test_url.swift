import Foundation
let baseUrl = "http://180.235.121.253/MyRajourney backend/core/public/index.php/api/v1/"
let path = "auth/login"
let components = URLComponents(string: baseUrl + path)
print("Components: \(String(describing: components))")

let baseUrl2 = "http://180.235.121.253/MyRajourney%20backend/core/public/index.php/api/v1/"
let components2 = URLComponents(string: baseUrl2 + path)
print("Components2 url: \(String(describing: components2?.url))")
