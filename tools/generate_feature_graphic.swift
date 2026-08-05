import AppKit
import Foundation

let output = CommandLine.arguments.dropFirst().first ?? "keydoku_production/assets/feature-graphic-1024x500.jpg"
let width: CGFloat = 1024
let height: CGFloat = 500

func color(_ hex: Int, alpha: CGFloat = 1.0) -> NSColor {
    let r = CGFloat((hex >> 16) & 0xff) / 255.0
    let g = CGFloat((hex >> 8) & 0xff) / 255.0
    let b = CGFloat(hex & 0xff) / 255.0
    return NSColor(calibratedRed: r, green: g, blue: b, alpha: alpha)
}

func drawText(_ text: String, in rect: NSRect, size: CGFloat, weight: NSFont.Weight, color textColor: NSColor, align: NSTextAlignment = .left) {
    let paragraph = NSMutableParagraphStyle()
    paragraph.alignment = align
    let attrs: [NSAttributedString.Key: Any] = [
        .font: NSFont.systemFont(ofSize: size, weight: weight),
        .foregroundColor: textColor,
        .paragraphStyle: paragraph
    ]
    NSString(string: text).draw(in: rect, withAttributes: attrs)
}

let image = NSImage(size: NSSize(width: width, height: height))
image.lockFocus()

color(0x101214).setFill()
NSBezierPath(rect: NSRect(x: 0, y: 0, width: width, height: height)).fill()

color(0x163245).setFill()
NSBezierPath(roundedRect: NSRect(x: -90, y: -130, width: 640, height: 640), xRadius: 240, yRadius: 240).fill()

color(0x2B6F83).setFill()
NSBezierPath(roundedRect: NSRect(x: 760, y: 270, width: 360, height: 320), xRadius: 130, yRadius: 130).fill()

let gridRect = NSRect(x: 84, y: 62, width: 376, height: 376)
let cell = gridRect.width / 3

color(0x3E7FE8).setFill()
NSBezierPath(rect: NSRect(x: gridRect.minX + cell, y: gridRect.minY + cell, width: cell, height: cell)).fill()

color(0xF5F7FA).setStroke()
let outer = NSBezierPath(roundedRect: gridRect, xRadius: 28, yRadius: 28)
outer.lineWidth = 12
outer.stroke()

for offset in [cell, cell * 2] {
    let v = NSBezierPath()
    v.move(to: NSPoint(x: gridRect.minX + offset, y: gridRect.minY))
    v.line(to: NSPoint(x: gridRect.minX + offset, y: gridRect.maxY))
    v.lineWidth = 8
    v.stroke()

    let h = NSBezierPath()
    h.move(to: NSPoint(x: gridRect.minX, y: gridRect.minY + offset))
    h.line(to: NSPoint(x: gridRect.maxX, y: gridRect.minY + offset))
    h.lineWidth = 8
    h.stroke()
}

func digit(_ value: String, row: Int, col: Int, size: CGFloat, textColor: NSColor) {
    let rect = NSRect(
        x: gridRect.minX + CGFloat(col) * cell,
        y: gridRect.maxY - CGFloat(row + 1) * cell + 10,
        width: cell,
        height: cell
    )
    drawText(value, in: rect, size: size, weight: .black, color: textColor, align: .center)
}

digit("1", row: 0, col: 0, size: 78, textColor: color(0xF5F7FA))
digit("9", row: 1, col: 1, size: 118, textColor: color(0xF5F7FA))
digit("8", row: 2, col: 2, size: 78, textColor: color(0xF5F7FA))

drawText("Keydoku", in: NSRect(x: 530, y: 255, width: 420, height: 92), size: 72, weight: .black, color: color(0xF5F7FA))
drawText("Offline Sudoku for keypad phones", in: NSRect(x: 535, y: 205, width: 430, height: 42), size: 26, weight: .bold, color: color(0xCFE7F0))
drawText("Built for small screens. No ads. No accounts.", in: NSRect(x: 535, y: 162, width: 430, height: 34), size: 20, weight: .medium, color: color(0xF5F7FA, alpha: 0.86))

image.unlockFocus()

guard let tiff = image.tiffRepresentation,
      let rep = NSBitmapImageRep(data: tiff),
      let data = rep.representation(using: .jpeg, properties: [.compressionFactor: 0.94]) else {
    fatalError("failed to encode jpeg")
}

try data.write(to: URL(fileURLWithPath: output))
