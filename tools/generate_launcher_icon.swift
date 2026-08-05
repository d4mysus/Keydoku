import AppKit
import Foundation

let output = CommandLine.arguments.dropFirst().first ?? "/private/tmp/keydoku_icon_master_v2.png"
let size: CGFloat = 1024

func color(_ hex: Int, alpha: CGFloat = 1.0) -> NSColor {
    let r = CGFloat((hex >> 16) & 0xff) / 255.0
    let g = CGFloat((hex >> 8) & 0xff) / 255.0
    let b = CGFloat(hex & 0xff) / 255.0
    return NSColor(calibratedRed: r, green: g, blue: b, alpha: alpha)
}

let image = NSImage(size: NSSize(width: size, height: size))
image.lockFocus()

NSColor.clear.setFill()
NSBezierPath(rect: NSRect(x: 0, y: 0, width: size, height: size)).fill()

let circleRect = NSRect(x: 36, y: 36, width: size - 72, height: size - 72)
color(0x101214).setFill()
NSBezierPath(ovalIn: circleRect).fill()

let gridSide: CGFloat = 676
let gridRect = NSRect(
    x: (size - gridSide) / 2,
    y: (size - gridSide) / 2,
    width: gridSide,
    height: gridSide
)
let cell = gridSide / 3.0

let highlightRect = NSRect(x: gridRect.minX + cell, y: gridRect.minY + cell, width: cell, height: cell)
color(0x3E7FE8).setFill()
NSBezierPath(rect: highlightRect).fill()

let outer = NSBezierPath(roundedRect: gridRect, xRadius: 40, yRadius: 40)
outer.lineWidth = 18
color(0xF5F7FA).setStroke()
outer.stroke()

for offset in [cell, cell * 2] {
    let vertical = NSBezierPath()
    vertical.move(to: NSPoint(x: gridRect.minX + offset, y: gridRect.minY))
    vertical.line(to: NSPoint(x: gridRect.minX + offset, y: gridRect.maxY))
    vertical.lineWidth = 14
    vertical.lineCapStyle = .round
    vertical.stroke()

    let horizontal = NSBezierPath()
    horizontal.move(to: NSPoint(x: gridRect.minX, y: gridRect.minY + offset))
    horizontal.line(to: NSPoint(x: gridRect.maxX, y: gridRect.minY + offset))
    horizontal.lineWidth = 14
    horizontal.lineCapStyle = .round
    horizontal.stroke()
}

let paragraph = NSMutableParagraphStyle()
paragraph.alignment = .center

func drawDigit(_ digit: String, row: Int, col: Int, size: CGFloat, color: NSColor) {
    let cellRect = NSRect(
        x: gridRect.minX + CGFloat(col) * cell,
        y: gridRect.maxY - CGFloat(row + 1) * cell,
        width: cell,
        height: cell
    )
    let attrs: [NSAttributedString.Key: Any] = [
        .font: NSFont.systemFont(ofSize: size, weight: .black),
        .foregroundColor: color,
        .paragraphStyle: paragraph
    ]
    let text = NSString(string: digit)
    let textSize = text.size(withAttributes: attrs)
    let drawRect = NSRect(
        x: cellRect.minX,
        y: cellRect.midY - textSize.height / 2 - 8,
        width: cellRect.width,
        height: textSize.height + 12
    )
    text.draw(in: drawRect, withAttributes: attrs)
}

drawDigit("1", row: 0, col: 0, size: 120, color: color(0xF5F7FA))
drawDigit("9", row: 1, col: 1, size: 196, color: color(0xF5F7FA))
drawDigit("8", row: 2, col: 2, size: 120, color: color(0xF5F7FA))

image.unlockFocus()

guard let tiff = image.tiffRepresentation,
      let rep = NSBitmapImageRep(data: tiff),
      let data = rep.representation(using: .png, properties: [:]) else {
    fatalError("failed to encode png")
}

try data.write(to: URL(fileURLWithPath: output))
