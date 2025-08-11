import SwiftUI
import shared

struct ContentView: View {
    var body: some View {
        VStack(spacing: 12) {
            Text("FinanceAnalyzer iOS")
                .font(.title)
            Text("KMP shared: " + (SharedFacade().moneyFromDouble(value: 123.45, currencyCode: "USD").toString()))
                .font(.footnote)
        }
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}


