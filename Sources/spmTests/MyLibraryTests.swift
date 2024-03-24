// MyLibraryTests.swift
import XCTest
import PowerAssert
@testable import MyLibrary

final class MyLibraryTests: XCTestCase {
  func testExample() {
    let a = 7
    let b = 4
    let c = 12

    #assert(max(a, b) == c)
  }
}
